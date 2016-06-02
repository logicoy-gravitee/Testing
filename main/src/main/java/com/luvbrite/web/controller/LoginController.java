package com.luvbrite.web.controller;

import java.util.Calendar;
import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.luvbrite.dao.LogDAO;
import com.luvbrite.dao.PasswordResetDAO;
import com.luvbrite.dao.UserDAO;
import com.luvbrite.utils.Exceptions;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.Log;
import com.luvbrite.web.models.PasswordReset;
import com.luvbrite.web.models.User;


@Controller
public class LoginController {
	
	private static Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	@Autowired
	private UserDAO dao;
	
	@Autowired
	private PasswordResetDAO pwdresetDAO;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	private LogDAO logDao;


	@RequestMapping(value = "/login")
	public ModelAndView login(
			
			@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "ret", required = false) String returnURL,
			@RequestParam(value = "logout", required = false) String logout) {

			ModelAndView model = new ModelAndView();
			
			if (error != null) {
				model.addObject("error", "Invalid username and password!");
			}

			if (logout != null) {
				model.addObject("msg", "You've been logged out successfully.");
			}


			if (returnURL != null) {
				model.addObject("ret", returnURL);
			}
			
			model.setViewName("login");

			return model;	
	}
	

	@RequestMapping(value = "/resetrequest", method = RequestMethod.GET)
	public String requestReset(ModelMap model){

		model.addAttribute("type", "request");
		
		return "reset";
	}
	

	@RequestMapping(value = "/createreset", method = RequestMethod.GET)
	public @ResponseBody GenericResponse createResetCode(
			@RequestParam(value="u", required=false) String username,
			@RequestParam(value="e", required=false) String email){
			
		GenericResponse r = new GenericResponse();
		r.setSuccess(false);
		
		if(username == null && email == null){
			r.setMessage("Invalid username/email");
			return r;
		}
		
		ObjectId code = null;
		
		if(username != null && !username.trim().equals("")){
			
			User u = dao.createQuery().field("username")
					.equal(username).retrievedFields(true, "username", "email")
					.get();
			
			if(u!=null){
				
				PasswordReset pr = new PasswordReset();
				pr.setEmail(u.getEmail());
				pr.setUsername(u.getUsername());
				pr.setDate(Calendar.getInstance().getTime());
				
				pwdresetDAO.save(pr);
				
				code = pr.get_id();
				
				System.out.println(" Code u - " + code);
				r.setSuccess(true);
			}
			else{
				
				r.setMessage("No valid username found");
			}
		}
		
		else if(email != null && !email.trim().equals("")){
			
			User u = dao.createQuery().field("email")
					.equal(email).retrievedFields(true, "username", "email")
					.get();
			
			if(u!=null){
				
				PasswordReset pr = new PasswordReset();
				pr.setEmail(u.getEmail());
				pr.setUsername(u.getUsername());
				pr.setDate(Calendar.getInstance().getTime());
				
				pwdresetDAO.save(pr);
				
				code = pr.get_id();
				
				System.out.println(" Code e - " + code);				
				r.setSuccess(true);
			}
			else{
				
				r.setMessage("No valid email found");
			}
		}
		
		else{
			
			r.setMessage("No valid user found");
		}
		
		return r;
	}
	

	@RequestMapping(value = "/reset/{code}", method = RequestMethod.GET)
	public String resetPassword(ModelMap model, @PathVariable ObjectId code){
		
		if(code == null){
			model.addAttribute("msg", "There was some error accessing this page");
			return "403";
		}
		
		model.addAttribute("type", "password");
		
		PasswordReset pr = pwdresetDAO.get(code);
		if(pr == null){
			model.addAttribute("msg", "Not a valid reset code");
		}
		
		else{
			
			model.addAttribute("username", pr.getUsername());
			model.addAttribute("email", pr.getEmail());
		}
		
		return "reset";
	}
	

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public String register(){
		return "register";
	}

	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public @ResponseBody GenericResponse createUser(
			@Validated @RequestBody User user, 
			BindingResult result){
		
		GenericResponse r = new GenericResponse();		
		r.setSuccess(false);
		
		if(result.hasErrors()){
			
			StringBuilder errMsg = new StringBuilder();
			
			List<FieldError> errors = result.getFieldErrors();
			for (FieldError error : errors ) {
				 errMsg
				 .append(" - ")
				 .append(error.getDefaultMessage())
				 .append("<br />");
			}
			
			r.setMessage(errMsg.toString());
		
		}
		
		else {
			
			/**
			 * Before creating the user, we need to make sure that the 
			 * email and username are unique
			 **/
			
			boolean usernameUnique = false,
					emailUnique = false,
					proceed = true;
			
			User u1 = dao.findOne("username", user.getUsername());
			if(u1==null) usernameUnique = true;
			

			User u2 = dao.findOne("email", user.getEmail());
			if(u2==null) emailUnique = true;
			
			
			if(user.getIdentifications() == null){
				proceed = false;
				r.setMessage("Please provide your ID card and doctors recommendation letter.");
			}
			
			else if(user.getIdentifications().getIdCard() == null 
					|| user.getIdentifications().getIdCard().equals("")){
				
				proceed = false;
				r.setMessage("Please provide your ID card.");
				
			}
			
			else if(user.getIdentifications().getRecomendation() == null 
					|| user.getIdentifications().getRecomendation().equals("")){
				
				proceed = false;
				r.setMessage("Please provide doctors recommendation letter.");
				
			}
			
			
			if(proceed && emailUnique && usernameUnique){
				
				//Generate userId
				long userId = dao.getNextSeq();
				if(userId != 0l){
					
					user.set_id(userId);
					user.setActive(false);
					user.setRole("customer");
					user.setDateRegistered(Calendar.getInstance().getTime());
					
					//Encode the password before saving it
					String encodedPwd = encoder.encode(user.getPassword());
					user.setPassword(encodedPwd);

					dao.save(user);
					
					
					/**
					 * Update Log
					 * */
					try {
						
						Log log = new Log();
						log.setCollection("users");
						log.setDetails("user created.");
						log.setDate(Calendar.getInstance().getTime());
						log.setKey(userId);
						
						logDao.save(log);						
					}
					catch(Exception e){
						logger.error(Exceptions.giveStackTrace(e));
					}
					
					
					r.setSuccess(true);
					r.setMessage(userId + "");
				}

				else {
					
					r.setMessage("Error generating new User.");
				}			
			}

			
			else {

				if(proceed){
					
					if(!emailUnique && !usernameUnique){
						r.setMessage("User exist with this username and email");
					}

					else if(!emailUnique){
						r.setMessage("User exist with this email");
					}

					else {
						r.setMessage("User already exist with this username. Please provide a different username.");
					}
				}
			}
			
		}
		
		
		return r;		
	}
}

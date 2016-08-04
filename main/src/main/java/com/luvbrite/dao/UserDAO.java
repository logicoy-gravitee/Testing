package com.luvbrite.dao;

import java.util.List;
import java.util.regex.Pattern;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luvbrite.utils.SequenceGen;
import com.luvbrite.web.models.User;

@Service
public class UserDAO extends BasicDAO<User, Long> {

	@Autowired
	protected UserDAO(Datastore ds) {
		super(ds);
	}
	
	private void setFilters(Query<User> query, Pattern regExp){
		query.or(
				query.criteria("username").equal(regExp),
				query.criteria("email").equal(regExp),
				query.criteria("fname").equal(regExp),
				query.criteria("lname").equal(regExp)
				);
	}
	
	
	public long count(String search, String status){
		
		if(!search.equals("")) {
			Pattern regExp = Pattern.compile(search + ".*", Pattern.CASE_INSENSITIVE);	
			Query<User> query = getDs().createQuery(getEntityClass());
			setFilters(query, regExp);
			
			if(status.equals("inactive")){
				query.field("active").equal(false);
			}
			else if(status.equals("active")){
				query.field("active").equal(true);
			}
			
			return count(query);	
		}
		else if(!status.equals("all")){

			Query<User> query = getDs().createQuery(getEntityClass());
			if(status.equals("inactive")){
				query.field("active").equal(false);
			}
			else if(status.equals("active")){
				query.field("active").equal(true);
			}
			
			return count(query);
		}
		
		else
			return super.count();
	}
	
	
	public List<User> find(String orderBy, int limit, int offset, String search, String status){
		
		final Query<User> query = getDs().createQuery(getEntityClass()).order(orderBy);

		//Apply search criteria
		if(!search.equals("")){
			Pattern regExp = Pattern.compile(search + ".*", Pattern.CASE_INSENSITIVE);
			setFilters(query, regExp);
		}
		
		if(status.equals("inactive")){
			query.field("active").equal(false);
		}
		else if(status.equals("active")){
			query.field("active").equal(true);
		}
		
		//Apply limit if applicable
		if(limit != 0) query.limit(limit);

		//Apply offset
		query.offset(offset);
		
		
		return query.asList();			
	}
	
	
	public long getNextSeq() {
		return SequenceGen.getNextSequence(getDs(), "users__id");
	}

}

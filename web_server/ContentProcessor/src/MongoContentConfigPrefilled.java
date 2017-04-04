

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;


public class MongoContentConfigPrefilled extends Configuration{
	  
	    public String host = "67.219.147.170";

	    public int port = 27000;

	 
	    public String db = "content";

	    public String user = "___contentDBuser";

	    public String password = "thisPWgivescontentACCEs";
	
}

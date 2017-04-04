

import java.util.Iterator;
import java.util.List;

import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.DBQuery.Query;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.portol.common.model.content.Content;
import com.portol.common.model.content.Content.Type;
import com.portol.common.model.content.ContentSearchRequest.Sort;

public class ContentRepository {

	public static final int RESULTS_PER_PAGE = 10;

	//the DB we are using
	private JacksonDBCollection<Content, String> contentMongo;

	public ContentRepository(MongoManaged contentMongoManaged){
		this.contentMongo = JacksonDBCollection.wrap(contentMongoManaged.getDB().getCollection("content"), Content.class, String.class);
	}

	public Content findByVideoKey(String videoKey) throws Exception {

		Query query = DBQuery.is("contentKey", videoKey);
		DBCursor<Content> cursor = contentMongo.find(query);

		if(cursor.count() != 1){
			throw new Exception("cant find one piece of mathcing content! content matches: " + cursor.count());
		}
		return cursor.toArray().get(0);
	}

	public Content findById(String contentId) {

		return contentMongo.findOneById(contentId);
	}

	//TODO use apache solr or something like that 
	public List<Content> textSearch(Sort sort_criteria, int pageIndex, String word){
		final BasicDBObject textSearchCommand = new BasicDBObject();
	    textSearchCommand.put("text", contentMongo.getName());
	    textSearchCommand.put("search", word);
	    final CommandResult commandResult = contentMongo.getDB().command(textSearchCommand);
	    
	    BasicDBList results = (BasicDBList)commandResult.get("results");
	    for( Iterator< Object > it = results.iterator(); it.hasNext(); )
	    {
	        BasicDBObject result  = (BasicDBObject) it.next();
	        BasicDBObject dbo = (BasicDBObject) result.get("obj");
	        System.out.println(dbo);
	    }
	    
	    return null;
	    
	}
	
	public List<Content> getAllContent(Sort sort_criteria, int pageIndex){

		BasicDBObject sort = null;
		if(sort_criteria == null){
			sort_criteria = Sort.UNSORTED;
		}
		
		switch(sort_criteria){
		case UNSORTED:
			break;
		case VIEW_COUNT_ASCEND:
			sort = new BasicDBObject("numPlays", 1);
			break;
		case VIEW_COUNT_DSCEND:
			sort = new BasicDBObject("numPlays", 0);
			break;
		default:
			break;

		}

		BasicDBObject query = new BasicDBObject("preview", false);

		DBCursor<Content> allContent = contentMongo.find(query);

		//sort if necessary
		if(sort != null){
			allContent.sort(sort).limit((pageIndex + 1) * RESULTS_PER_PAGE);
		} else {
			allContent.limit((pageIndex + 1) * RESULTS_PER_PAGE);
		} 

		//toss the elements we are not interested in
		allContent.skip(pageIndex  * RESULTS_PER_PAGE);

		return allContent.toArray();



	}

	public List<Content> getAllWithType(Sort sortType, int pageIndex, Type targetType) {
		
		BasicDBObject sort = null;
		switch(sortType){
		case UNSORTED:
			break;
		case VIEW_COUNT_ASCEND:
			sort = new BasicDBObject("numPlays", 1);
			break;
		case VIEW_COUNT_DSCEND:
			sort = new BasicDBObject("numPlays", 0);
			break;
		default:
			break;

		}

		BasicDBObject query = new BasicDBObject("preview", false).append("type", targetType);

		DBCursor<Content> allContent = contentMongo.find(query);

		//sort if necessary
		if(sort != null){
			allContent.sort(sort).limit((pageIndex + 1) * RESULTS_PER_PAGE);
		} else {
			allContent.limit((pageIndex + 1) * RESULTS_PER_PAGE);
		} 

		//toss the elements we are not interested in
		allContent.skip(pageIndex  * RESULTS_PER_PAGE);

		return allContent.toArray();

	}

	public void save(Content content) {
		contentMongo.save(content);
		
	}







}

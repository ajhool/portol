package com.portol.contentserver.repository;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.Charsets;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;
import com.mongodb.BasicDBObject;
import com.portol.common.model.Category;
import com.portol.common.model.content.Content;
import com.portol.contentserver.manager.MongoManaged;

public class CategoryRepository {

	// the DB we are using
	private JacksonDBCollection<Category, String> catMongo;

	private static Logger logger = LoggerFactory.getLogger(CategoryRepository.class);

	public CategoryRepository(MongoManaged catMongoManaged) {
		this.catMongo = JacksonDBCollection.wrap(catMongoManaged.getDB().getCollection("category"), Category.class,
				String.class);
	}

	public Category saveCategory(Category toSave) {
		// check for duplicate name
		BasicDBObject query = new BasicDBObject("name", toSave.getName());
		Category existing = catMongo.findOne(query);

		if (existing != null) {
			logger.warn("warning, overwriting existing category: " + existing.getName());
			catMongo.removeById(existing.getCategoryId());
		}

		WriteResult<Category, String> result = catMongo.save(toSave);

		if (result != null) {
			logger.info("successfully added new category: " + result.getSavedObject().getName());
		}
		
		return result.getSavedObject();
	}

	public List<Category> getAllValidCategories() throws IOException {

		DBCursor<Category> allCats = catMongo.find();

		return allCats.toArray();
	}

}

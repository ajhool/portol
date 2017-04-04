package com.portol.cloudplayer.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.portol.cloudplayer.manager.MongoUnManaged;

public class MongoHealthCheck extends HealthCheck {

        private MongoUnManaged mongo;

        public MongoHealthCheck(MongoUnManaged mongoManaged) {
            this.mongo = mongoManaged;
        }

        @Override
        protected Result check() throws Exception {
            mongo.getDB();
            return Result.healthy();
        }

}
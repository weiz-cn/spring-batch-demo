package com.example.spring_batch_demo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.MongoJobExplorerFactoryBean;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MongoJobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfiguration {

    @Bean
    public Job endOfDay(JobRepository jobRepository, Step step1) {
        return new JobBuilder("endOfDay", jobRepository)
                .start(step1)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step1", jobRepository)
                .tasklet((contribution, chunkContext) -> null, transactionManager)
                .build();
    }


    @Configuration
    @EnableBatchProcessing
    static class MongoJobRepositoryConfig {
        @Bean
        public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
            MongoTemplate template = new MongoTemplate(mongoDatabaseFactory);
            MappingMongoConverter converter = (MappingMongoConverter) template.getConverter();
            converter.setMapKeyDotReplacement(".");
            return template;
        }

        @Bean
        public JobRepository jobRepository(MongoTemplate mongoTemplate, MongoTransactionManager transactionManager)
                throws Exception {
            MongoJobRepositoryFactoryBean jobRepositoryFactoryBean = new MongoJobRepositoryFactoryBean();
            jobRepositoryFactoryBean.setMongoOperations(mongoTemplate);
            jobRepositoryFactoryBean.setTransactionManager(transactionManager);
            jobRepositoryFactoryBean.afterPropertiesSet();
            return jobRepositoryFactoryBean.getObject();
        }

        @Bean
        public JobExplorer jobExplorer(MongoTemplate mongoTemplate, MongoTransactionManager transactionManager)
                throws Exception {
            MongoJobExplorerFactoryBean jobExplorerFactoryBean = new MongoJobExplorerFactoryBean();
            jobExplorerFactoryBean.setMongoOperations(mongoTemplate);
            jobExplorerFactoryBean.setTransactionManager(transactionManager);
            jobExplorerFactoryBean.afterPropertiesSet();
            return jobExplorerFactoryBean.getObject();
        }

        @Bean
        public MongoTransactionManager transactionManager(MongoDatabaseFactory mongoDatabaseFactory) {
            MongoTransactionManager mongoTransactionManager = new MongoTransactionManager();
            mongoTransactionManager.setDatabaseFactory(mongoDatabaseFactory);
            mongoTransactionManager.afterPropertiesSet();
            return mongoTransactionManager;
        }
    }
}

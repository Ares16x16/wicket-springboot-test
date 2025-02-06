package com.tutorial.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.index.Term;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.apache.commons.text.RandomStringGenerator;

@Service
public class LuceneService {
    private static final String INDEX_DIR = "lucene-index";
    private final Analyzer standardAnalyzer = new StandardAnalyzer();
    private final Analyzer chineseAnalyzer = new SmartChineseAnalyzer();
    private Directory directory;

    @PostConstruct
    public void init() throws Exception {
        directory = FSDirectory.open(Paths.get(INDEX_DIR));
        indexContent();
    }

    // Renamed and modified to index dummy content instead of users
    public void indexContent() throws Exception {
        IndexWriterConfig config = new IndexWriterConfig(standardAnalyzer);
        IndexWriter writer = new IndexWriter(directory, config);
        writer.deleteAll(); // Clear existing index

        // List of dummy documents
        List<Document> documents = new ArrayList<>();
        
        // Dummy English content
        Document doc1 = new Document();
        doc1.add(new StringField("id", "1", Field.Store.YES));
        doc1.add(new TextField("title", "Hello World", Field.Store.YES));
        doc1.add(new TextField("snippet", "This is an English document.", Field.Store.YES));
        documents.add(doc1);
        
        // Dummy Chinese content
        Document doc2 = new Document();
        doc2.add(new StringField("id", "2", Field.Store.YES));
        doc2.add(new TextField("title", "你好", Field.Store.YES));
        doc2.add(new TextField("snippet", "现在 是 反对 然后 说 我想 再次 如 说 部分 为. 年 我 有 和 他 她 结束 两个. 之间 时间 看 在...上 也 意愿 是 我们 路径 他们 意愿. 说 是, 她 在...上 说 在 或 一点 这个.。", Field.Store.YES));
        documents.add(doc2);
        
        // Random word generator
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
            .withinRange('a', 'z')
            .get();
        RandomStringGenerator chineseGenerator = new RandomStringGenerator.Builder()
            .withinRange('\u4e00', '\u9fa5')
            .get();
        
        // Add more dummy documents with random words
        for (int i = 3; i <= 10; i++) {
            Document doc = new Document();
            doc.add(new StringField("id", String.valueOf(i), Field.Store.YES));
            String randomTitle = generator.generate(5) + " " + chineseGenerator.generate(2);
            String randomSnippet = chineseGenerator.generate(50);
            doc.add(new TextField("title", randomTitle, Field.Store.YES));
            doc.add(new TextField("snippet", randomSnippet, Field.Store.YES));
            documents.add(doc);
        }

        for (int i = 11; i <= 20; i++) {
            Document doc = new Document();
            doc.add(new StringField("id", String.valueOf(i), Field.Store.YES));
            String randomTitle = generator.generate(5) + " " + chineseGenerator.generate(2);
            String randomSnippet = generator.generate(50);
            doc.add(new TextField("title", randomTitle, Field.Store.YES));
            doc.add(new TextField("snippet", randomSnippet, Field.Store.YES));
            documents.add(doc);
        }
        
        // Add all documents to the index
        for (Document document : documents) {
            writer.addDocument(document);
        }
        
        writer.close();
    }
    
    // Method to fetch all content
    public List<SearchResult> getAllContent() throws Exception {
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        List<SearchResult> results = new ArrayList<>();

        for (int i = 0; i < reader.maxDoc(); i++) {
            Document doc = reader.document(i);
            String id = doc.get("id");
            String title = doc.get("title");
            String snippet = doc.get("snippet");
            float score = 1.0f; // Default score for all content
            results.add(new SearchResult(id, title, snippet, score));
        }
        reader.close();
        return results;
    }
    
    // Updated method to choose query based on search algorithm
    public List<SearchResult> searchContent(String searchTerm, String searchAlgo) throws Exception {
        // If search term is empty or null, return all content
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllContent();
        }

        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        Query query;
        Analyzer analyzer = chineseAnalyzer;
        
        searchTerm = searchTerm.trim().toLowerCase();
        
        if ("Fuzzy".equalsIgnoreCase(searchAlgo)) {
            // Tokenize and build fuzzy query
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            List<String> tokens = new ArrayList<>();
            try (TokenStream tokenStream = analyzer.tokenStream("", searchTerm)) {
                tokenStream.reset();
                while (tokenStream.incrementToken()) {
                    tokens.add(tokenStream.getAttribute(org.apache.lucene.analysis.tokenattributes.CharTermAttribute.class).toString());
                }
                tokenStream.end();
            }
            
            if (tokens.isEmpty()) {
                tokens.add(searchTerm); // Use the original term if no tokens
            }
            
            for (String token : tokens) {
                int maxEdits = token.length() <= 2 ? 1 : 2;
                builder.add(new FuzzyQuery(new Term("title", token), maxEdits), BooleanClause.Occur.SHOULD);
                builder.add(new FuzzyQuery(new Term("snippet", token), maxEdits), BooleanClause.Occur.SHOULD);
            }
            query = builder.build();
        } else {
            // Standard search using MultiFieldQueryParser for multi-word support
            String[] fields = {"title", "snippet"};
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
            // Optionally: parser.setDefaultOperator(QueryParser.Operator.AND);
            query = parser.parse(searchTerm);
        }

        TopDocs docs = searcher.search(query, 50);
        List<SearchResult> results = new ArrayList<>();

        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            Document doc = searcher.storedFields().document(scoreDoc.doc);
            String id = doc.get("id");
            String title = doc.get("title");
            String snippet = doc.get("snippet");
            float score = scoreDoc.score;
            results.add(new SearchResult(id, title, snippet, score));
        }
        reader.close();
        return results;
    }
    
    // Updated inner static class with id field
    public static class SearchResult implements Serializable {
        private final String id;
        private final String title;
        private final String snippet;
        private final float score;
        public SearchResult(String id, String title, String snippet, float score) {
            this.id = id;
            this.title = title;
            this.snippet = snippet;
            this.score = score;
        }
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getSnippet() { return snippet; }
        public float getScore() { return score; }
    }
}

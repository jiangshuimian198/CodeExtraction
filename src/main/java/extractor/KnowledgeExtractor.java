package main.java.extractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.yaml.snakeyaml.Yaml;

import lombok.Getter;
import lombok.Setter;

public abstract class KnowledgeExtractor {

    @Getter
    @Setter
    private String graphDir;

    @Getter
    @Setter
    private String dataDir;

    @Getter
    private BatchInserter inserter = null;
    @Getter
    private GraphDatabaseService db = null;

    public static void execute(List<ExtractorConfig> extractorConfigList) {
        for (ExtractorConfig config : extractorConfigList) {
            System.out.println(config.getClassName() + " start ...");
            KnowledgeExtractor extractor = null;
            try {
                extractor = (KnowledgeExtractor) Class.forName(config.getClassName()).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            extractor.setGraphDir(config.getGraphDir());
            extractor.setDataDir(config.getDataDir());
            try {
                extractor.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(config.getClassName() + " finished.");
        }
    }

    public static void executeFromYaml(String yamlStr) {
        Yaml yaml = new Yaml();
        Map<String, String> ret = yaml.load(yamlStr);
        String graphDir = ret.get("graphDir");
        ret.remove("graphDir");
        List<ExtractorConfig> configs = new ArrayList<>();
        for (String key : ret.keySet()) {
            configs.add(new ExtractorConfig(key, graphDir, ret.get(key)));
        }
        if (new File(graphDir).exists()){
            try {
                FileUtils.deleteDirectory(new File(graphDir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        execute(configs);
    }

    /*public static void main(String[] args) {
        try {
            KnowledgeExtractor.executeFromYaml(FileUtils.readFileToString(new File(args[0]), "utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    public static boolean extract(File yamlFile)
    {
    	try {
            KnowledgeExtractor.executeFromYaml(FileUtils.readFileToString(yamlFile, "utf-8"));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
		return false;
    }

    public boolean isBatchInsert() {
        return false;
    }

    public abstract void extraction();

    public void execute() throws IOException {
        if (this.isBatchInsert()) {
            inserter = BatchInserters.inserter(new File(graphDir));
            this.extraction();
            inserter.shutdown();
        } else {
            db = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphDir));
            this.extraction();
            db.shutdown();
        }
    }

}
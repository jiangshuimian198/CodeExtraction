package main.java.infos.statementinofs;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.neo4j.unsafe.batchinsert.BatchInserter;

import lombok.Getter;
import main.java.JCExtractor.JavaExtractor;
import main.java.infos.JavaExpressionInfo;

public class WhileStatementInfo extends JavaStatementInfo{
	@Getter
	private long nodeId;
	private HashMap<String, Object> map;

	public WhileStatementInfo(BatchInserter inserter, String belongTo, int statementNo, Statement statement) {
		super.belongTo=belongTo;
		super.statementNo=statementNo;
		super.setStatementType("WhileStatement");
		map = new HashMap<String, Object>();
		WhileStatement whileStatement = (WhileStatement)statement;
		nodeId = createNode(inserter);
		Expression loopCondition = whileStatement.getExpression();
		Statement whileBody = whileStatement.getBody();
		long loopConditionId = JavaExpressionInfo.createJavaExpressionInfo(inserter, loopCondition);
		if(loopConditionId!=-1)
		{
			inserter.createRelationship(nodeId, loopConditionId, JavaExtractor.LOOP_CONDITION, new HashMap<>());
		}
		long bodyId = JavaStatementInfo.createJavaStatementInfo(inserter, belongTo, statementNo, whileBody);
		if(bodyId!=-1)
		{
			inserter.createRelationship(nodeId, bodyId, JavaExtractor.STATEMENT_BODY, new HashMap<>());
		}
	}
	
	private long createNode(BatchInserter inserter) {
		super.addProperties(map);
        return inserter.createNode(map, JavaExtractor.STATEMENT);
    }
	
}

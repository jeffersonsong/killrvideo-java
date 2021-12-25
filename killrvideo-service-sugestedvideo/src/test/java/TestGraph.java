import com.datastax.dse.driver.api.core.graph.FluentGraphStatement;
import com.datastax.dse.driver.api.core.graph.GraphNode;
import com.datastax.dse.driver.api.core.graph.GraphResultSet;
import com.datastax.oss.driver.api.core.CqlSession;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import static com.datastax.dse.driver.api.core.graph.DseGraph.g;

public class TestGraph {
    public static void main(String[] args) {
        CqlSession session = CqlSession.builder().build();

        GraphTraversal<Vertex, Vertex> traversal = g.V().has("name", "marko");
        FluentGraphStatement statement = FluentGraphStatement.newInstance(traversal);

        GraphResultSet result = session.execute(statement);
        for (GraphNode node : result) {
            System.out.println(node.asVertex());
        }
    }
}

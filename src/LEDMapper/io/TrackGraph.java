/*
 * Nodes can have multiple incoming and outgoing edges, therefore a graph structure is appropriate.
 */

package LEDMapper.io;

import LEDMapper.components.PCBSide;
import LEDMapper.util.ExactPoint;

import java.math.BigDecimal;
import java.util.*;

class Node {
    ExactPoint location;
    Set<Edge> connectedEdges;
    PCBSide side;

    public Node(BigDecimal x, BigDecimal y, PCBSide side){
        this.location = new ExactPoint(x,y);
        this.side = side;
        connectedEdges = new HashSet<>();
    }

    public Node(ExactPoint location, PCBSide side) {
        this.location = location;
        this.side = side;
        connectedEdges = new HashSet<>();
    }

    public boolean equals(Object o){
        if ( !(o instanceof Node other ) ) return false;
        return location.equals(other.location) && this.side == other.side;
    }

    public int hashCode(){
        return location.hashCode() + side.hashCode();
    }

    public ExactPoint getLocation() {
        return location;
    }

    public void addConnectedEdge(Edge e){
        connectedEdges.add(e);
    }

    // Follows edges one-hop.
    public Set<Node> getConnectedNodes(){
        Set<Node> nodes = new HashSet<>(); /// Doesn't work for vias!
        for (Edge e : connectedEdges){
            nodes.add(e.getOpposingNode(this));
        }
        return nodes;
    }

    // Follows edges as many hops as needed until they end, returning the full set of nodes traversed.
    public Set<Node> getConnectedNodesClosure(){
        Set<Node> closure = new HashSet<>();
        closure.add(this);
        Stack<Node> toProcess = new Stack<>();
        toProcess.addAll(getConnectedNodes());

        while (!toProcess.isEmpty()){
            Node next = toProcess.pop();
            if (!closure.contains(next)) {
                closure.add(next);
                Set<Node> attached = next.getConnectedNodes();
                attached.removeAll(closure);
                toProcess.addAll(attached);
            }
        }

        closure.remove(this);
        return closure;
    }
}

class Edge {
    Node start;
    Node end;

    public Edge(Node start, Node end){
        this.start = start;
        this.end = end;
    }

    // This is equality for undirected edges. It doesn't actually matter which way they are going!
    public boolean equals(Object o){
        if ( !(o instanceof Edge other ) ) return false;
        return (start.equals(other.start) && end.equals(other.end))
                || (end.equals(other.start) & start.equals(other.end));
    }

    public Node getOpposingNode(Node n){
        if (n.equals(start)) return end;
        else return start;
    }
}

public class TrackGraph {
    // Nodes map to themselves, allowing for constant time lookup of nodes which .equal the node of interest,
    // where Node equality is based solely on location.
    Map<Node,Node> frontNodes;
    Map<Node,Node> backNodes;

    public TrackGraph(){
        frontNodes = new HashMap<>();
        backNodes = new HashMap<>();
    }

    public void addSegment(ExactPoint pointA, ExactPoint pointB, PCBSide side){
        Node start = new Node(pointA, side);
        Node end = new Node(pointB, side);

        Map<Node,Node> nodes; // Set appropriately for side of pcb below.
        if (side == PCBSide.FRONT) {
            nodes = frontNodes;
        }
        else {
            nodes = backNodes;
        }

        // Only add a node if it's our first time seeing it! Otherwise update our variable.
        if (nodes.containsKey(start)) { start = nodes.get(start); }
        else { nodes.put(start,start); }
        if (nodes.containsKey(end)) { end = nodes.get(end); }
        else { nodes.put(end,end); }
        Edge e = new Edge(start, end);
        start.addConnectedEdge(e);
        end.addConnectedEdge(e);
    }

    // A via is really just an edge connecting two nodes, where the two nodes are on different surfaces.
    public void addVia(ExactPoint loc, PCBSide side1, PCBSide side2){
        Node lookupNode1 = new Node(loc, side1);
        Node lookupNode2 = new Node(loc, side2);

        if((frontNodes.containsKey(lookupNode1) || backNodes.containsKey(lookupNode1))
            && (frontNodes.containsKey(lookupNode2) || backNodes.containsKey(lookupNode2))) {

            Node fNode;
            Node bNode;

            if (frontNodes.containsKey(lookupNode1)) {
                fNode = frontNodes.get(lookupNode1);
                bNode = backNodes.get(lookupNode2);
            } else {
                fNode = backNodes.get(lookupNode2);
                bNode = frontNodes.get(lookupNode1);
            }

            Edge e = new Edge(fNode, bNode);
            fNode.addConnectedEdge(e);
            bNode.addConnectedEdge(e);
        }
    }

    public Node getNode(ExactPoint location, PCBSide side){
        if (side == PCBSide.FRONT) return frontNodes.get(new Node(location, side));
        return backNodes.get(new Node(location, side));
    }

    public boolean hasNode(ExactPoint location, PCBSide side){
        if (side == PCBSide.FRONT) return frontNodes.containsKey(new Node(location, side));
        return backNodes.containsKey(new Node(location, side));
    }

    /**
     * Get all of the points connected to location through the track graph.
     * @param location
     * @return
     */
    public List<ExactPoint> connectionPoints(ExactPoint location, PCBSide side){
        List<ExactPoint> points = new ArrayList<>();
        Node node = getNode(location, side);

        if (node == null) return points;

        for (Node n : node.getConnectedNodesClosure()){
            points.add(n.getLocation());
        }
        return points;
    }
}

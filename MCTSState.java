import java.util.PriorityQueue;

public class MCTSState implements Comparable<MCTSState>{

    // atributes
    private Board cur_b;
    private int player;
    private MCTSState parent;
    private PriorityQueue<MCTSState> children;
    private double u, n;

    // constructor
    MCTSState(Board b, int play, MCTSState p){
        cur_b = b;
        player = play;
        parent = p;
        children = new PriorityQueue<MCTSState>();
        u = 0; n = 0;
    }

    // getters
    public Board getBoardObject(){return cur_b;}
    public int getPlayer(){return player;}
    public MCTSState getParent(){return parent;}

    public PriorityQueue<MCTSState> getChildren(){return children;}
    public PriorityQueue<MCTSState> getChildrenCopy(){
        PriorityQueue<MCTSState> copy = new PriorityQueue<>();
        for (MCTSState s : children)
            copy.add(s);
        return copy;
    }

    public double getU(){return u;}
    public double getN(){return n;}

    // modifiers
    public void addToU(int x){u += x;}
    public void addToN(int x){n += x;}
    public void setChildren(PriorityQueue<MCTSState> new_children){children = new_children;}

    // calculate Upper Confindence Bound for Trees (UCT)
    public double getUCT(){
        // hasn't been explored yet
        if (this.getN() == 0) return (double) MCTSConstants.INF;
        // else
        double uct = (
            this.getU()/this.getN() + 
            MCTSConstants.EXPLORATION * Math.sqrt(
                Math.log10(this.parent.getN()) / this.getN()
            )
        );
        return uct;
    }
    
    // comparador
    @Override
    public int compareTo(MCTSState other){
        return -1 * Double.compare(this.getUCT(), other.getUCT());
    }
}

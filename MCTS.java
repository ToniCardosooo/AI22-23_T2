import java.util.ArrayList;
import java.util.Collections;

public class MCTS{
    private MCTSState root;

    MCTS(Board r){
        root = new MCTSState(r, 2, null);
    }

    // function that gets all the available moves for that particular turn
    private ArrayList<Integer> getValidMoves(Board board){
        ArrayList<Integer> moves = new ArrayList<Integer>();
        for(int i=0; i<7; i++){
            if(!board.verifyColumnFull(i)) moves.add(i);
        }
        Collections.shuffle(moves);
        return moves;
    }

    private MCTSState selection(){
        MCTSState cur = root;

        // while it's not a leaf
        while (cur.getChildren().size() > 0){
            ArrayList<MCTSState> cur_children = cur.getChildrenCopy();
            Collections.sort(cur_children);

            // gather in the list all the best nodes
            ArrayList<MCTSState> max_nodes = new ArrayList<MCTSState>();
            double max_value = cur_children.get(0).getUCT();
            for (int i = 0; i < cur_children.size(); i++){
                if (cur_children.get(i).getUCT() < max_value) break;
                max_nodes.add(cur_children.get(i));
            }
            
            // choose a random best node
            Collections.shuffle(max_nodes);
            cur = max_nodes.get(0);

            // if it hasn't been explored yet
            if (cur.getN() == 0) return cur;
        }

        // cur is a leaf
        return cur;
    }

    private MCTSState expand(MCTSState leaf){
        if (Heuristics.isFinished(leaf.getBoardObject()))
            return null;

        Board leaf_b = leaf.getBoardObject();
        int player = leaf.getPlayer();
        ArrayList<MCTSState> leaf_children = leaf.getChildren();

        // create children
        for (int i : getValidMoves(leaf_b)){
            Board aux = leaf_b.makeMove(i, player);
            aux.setParent(leaf_b);
            MCTSState child = new MCTSState(aux, 3-player, leaf);
            leaf_children.add(child);
        }

        // randomly choose and return a child node
        Collections.shuffle(leaf_children);
        return leaf_children.get(0);
    }   

    private int rollout(MCTSState child){
        // simulate
        Board cur = child.getBoardObject();
        int player = child.getPlayer();

        // randomly play
        while (!Heuristics.isFinished(cur)){
            int rand_col = getValidMoves(cur).get(0);
            cur = cur.makeMove(rand_col, player);
            player = 3-player;
        }

        // return the player that won
        return 3-player;
    }

    private void backpropagate(MCTSState child, int outcome){
        int reward = 1;
        if (outcome == child.getPlayer()) reward = 0;

        while (child != null){
            child.addToN(1);
            child.addToU(reward);

            // reorder the children list on the child's parent
            if (child.getParent() != null)
                Collections.sort(child.getParent().getChildren());
                
            child = child.getParent();
            // if it is a draw, count as loss for every state
            if (outcome == 0) reward = 0;
            else reward = 1-reward;
        }
    }

    public int playMCTS(){
        for (int i = 0; i < MCTSConstants.MCTS_ITERATIONS; i++){
            MCTSState leaf = selection();
            
            // initialize with a value in case the leaf is terminal
            // leaf does have a child
            if (!Heuristics.isFinished(leaf.getBoardObject())){
                MCTSState child = expand(leaf);
                int rollout_value = rollout(child);
                backpropagate(child, rollout_value);
            }
            // leaf is a final state
            else{
                int outcome = 0; // board full and tie
                if (Heuristics.getScore(leaf.getBoardObject()) == +512) outcome = 1; // X won
                else if (Heuristics.getScore(leaf.getBoardObject()) == -512) outcome = 2; // O won
                backpropagate(leaf, outcome);
            }   
        }

        // move with highest UCT
        MCTSState result = root.getChildren().get(0);
        return result.getBoardObject().getLastMove();
    }
}

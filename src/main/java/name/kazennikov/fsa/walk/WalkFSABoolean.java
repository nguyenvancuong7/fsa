package name.kazennikov.fsa.walk;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import name.kazennikov.fsa.Constants;
import name.kazennikov.fsa.IntFSABooleanEventHandler;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class WalkFSABoolean extends BaseWalkFSA {

    BitSet finals = new BitSet();
    private final long serialVersionUUID = 2L;
    public static final int ANNOTATION_LABEL = 0;


	public static class Builder implements IntFSABooleanEventHandler {
		WalkFSABoolean fsa = new WalkFSABoolean();
		boolean isFinal;
		int state;
		@Override
		public void startState(int state) {
			fsa.stateStart.add(fsa.labels.size());
			this.state = state;
		}

		@Override
		public void setFinal() {
			if(isFinal)
				fsa.finals.set(state);
		}

		@Override
		public void addTransition(int label, int destState) {
			fsa.labels.add(label);
			fsa.dest.add(destState);
		}

		@Override
		public void endState() {			
		}

		@Override
		public void setFinalValue(boolean value) {
			isFinal = value;
		}
		
		public WalkFSABoolean build() {
			fsa.dest.trimToSize();
			fsa.labels.trimToSize();
			fsa.stateStart.trimToSize();
			return fsa;
		}


	}

    /**
     * Check if state have an annotation transitiion
     * (transition with annotation label)
     * @param state state to check
     */
    public boolean hasAnnotStart(int state) {
        return next(state, ANNOTATION_LABEL) != Constants.INVALID_STATE;
    }

    public boolean isFinalState(int state) {
        return finals.get(state);
    }

    /**
     * Collect annotations starting at the state
     * @param state start state
     * @param needSep true if needed a separator to output list
     * @param sepValue separator value
     *
     * @return annotation data
     */
    public TIntArrayList collectAnnotationsSimple(int state, boolean needSep, int sepValue) {
        TIntArrayList annots = new TIntArrayList();

        if(!hasAnnotStart(state))
            return annots;

        TIntArrayList data = new TIntArrayList();
        collectAnnotations(next(state, 0), data, true, annots, needSep, sepValue);
        return annots;
    }

    public TIntArrayList collectAnnotationsSimple(int state) {
        return collectAnnotationsSimple(state, false, 0);
    }



    /**
     * Collect annotations starting at this (or at any descendent) state
     * @param state start state
     * @param annots output list for annotations
     * @param addSep if true, add specified separator to the output list
     * @param sepValue separator value for output list
     *
     * @return annotation data
     */
    public TIntArrayList collectAnnotations(int state, TIntArrayList annots, boolean addSep, int sepValue) {
        TIntArrayList data = new TIntArrayList();
        collectAnnotations(state, data, false, annots, addSep, sepValue);
        return annots;
    }



    /**
     * Recursively collect annotation from the FSA
     * @param state start state
     * @param data current annotation data
     * @param passedAnnotChar flag of passing the annotation char in the FSA
     * @param annots output list for the annotations
     * @param addSep if true, add specified separator to the output list
     * @param sepValue separator value for output list
     *
     * @return annotation data
     */
    public TIntArrayList collectAnnotations(int state, TIntArrayList data, boolean passedAnnotChar, TIntArrayList annots, boolean addSep, int sepValue) {

        if(finals.get(state)) {
            for(int i = 0; i < data.size(); i++) {
                annots.add(data.get(i));
            }

            if(addSep)
                annots.add(sepValue);
        }

        int start = stateStart.get(state);
        int end = state < stateStart.size() - 1? stateStart.get(state + 1) : stateStart.size();

        while(start < end) {
            int input = labels.get(start);
            if(passedAnnotChar)
                data.add(labels.get(start));

            collectAnnotations(dest.get(start), data, input == 0? true : passedAnnotChar, annots, addSep, sepValue);

            if(passedAnnotChar)
                data.removeAt((data.size() - 1));
            start++;
        }

        return annots;
    }

}

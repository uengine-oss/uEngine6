package org.uengine.kernel.bpmn;

import org.uengine.kernel.Activity;
import org.uengine.kernel.ProcessInstance;

import java.util.*;

public class BlockFinder {

    Activity joinActivity;
    Activity startEvent;

    public Activity getJoinActivity() {
        return joinActivity;
    }

    public void setJoinActivity(Activity joinActivity) {
        this.joinActivity = joinActivity;
    }

    List<Activity> blockMembers = new ArrayList<Activity>();

    public List<Activity> getBlockMembers() {
        return blockMembers;
    }

    int branch = 1;

    private BlockFinder(Activity joinActivity) {
        setJoinActivity(joinActivity);
        setStartEvent(joinActivity);
        visitForDepthAndVisitCountSetting(joinActivity);
        visitToLineUp(joinActivity);
        findBlockMembers();
    }

    // protected void find(Activity activity){
    // branch += (activity.getIncomingTransitions().size()-1);
    //
    // //Since queue is a interface
    // Queue<Activity> queue = new LinkedList<Activity>();
    //
    // if(activity == null) return;
    //
    // //Adds to end of queue
    // queue.add(activity);
    //
    // while(!queue.isEmpty())
    // {
    // //removes from front of queue
    // Activity r = queue.remove();
    //
    // blockMembers.put(r.getTracingTag(), r);
    //
    // //Visit child first before grandchild
    // for(Transition transition : r.getIncomingTransitions())
    // {
    // Activity sourceActivity = transition.getSourceActivity();
    //
    // if(!blockMembers.containsKey(sourceActivity.getTracingTag()))
    // {
    // blockMembers.put(sourceActivity.getTracingTag(), sourceActivity);
    //
    // queue.add(sourceActivity);
    // }
    // }
    //
    // if(queue.size()==1) return;
    // }
    //
    // }

    public void setStartEvent(Activity joinActivity) {
        this.startEvent = findStartEvent(joinActivity);
    }

    public Activity findStartEvent(Activity activity) {
        return findStartEventRecursive(activity, new HashSet<>());
    }

    private Activity findStartEventRecursive(Activity activity, Set<String> visited) {
        if (this.startEvent != null) {
            return this.startEvent;
        }

        if (activity == null || visited.contains(activity.getTracingTag())) {
            return null;
        }

        visited.add(activity.getTracingTag());

        if (activity instanceof StartEvent) {
            return activity;
        }

        for (SequenceFlow incomingFlow : activity.getIncomingSequenceFlows()) {
            Activity sourceActivity = incomingFlow.getSourceActivity();
            Activity startEvent = findStartEventRecursive(sourceActivity, visited);
            if (startEvent != null)
                return startEvent;
        }

        return null;
    }

    public int getDepthFromStartEvent(Activity activity) {
        if (startEvent == null)
            return -1;

        Map<Activity, Integer> depthCache = new HashMap<>();
        return calculateDepth(startEvent, activity, 0, new HashSet<>(), depthCache);
    }

    private int calculateDepth(Activity current, Activity target, int depth, Set<String> visited,
            Map<Activity, Integer> depthCache) {
        if (current == null || visited.contains(current.getTracingTag())) {
            return -1;
        }

        if (depthCache.containsKey(current)) {
            return depthCache.get(current);
        }

        if (current.equals(target))
            return depth;

        visited.add(current.getTracingTag());

        int maxDepth = -1;

        for (SequenceFlow outgoingFlow : current.getOutgoingSequenceFlows()) {
            Activity nextActivity = outgoingFlow.getTargetActivity();
            int result = calculateDepth(nextActivity, target, depth + 1, visited, depthCache);
            if (result > maxDepth) {
                maxDepth = result;
            }
        }

        depthCache.put(current, maxDepth);

        visited.remove(current.getTracingTag());

        return maxDepth;
    }

    Integer depth = 0;
    Map<Integer, List<Activity>> activitiesByDistanceMap = new HashMap<Integer, List<Activity>>();
    Map<String, Integer> distancesByActivity = new HashMap<String, Integer>();
    Map<String, Integer> visitCount = new HashMap<String, Integer>();
    Stack<Activity> visitActivityStack = new Stack<Activity>();

    protected void visitForDepthAndVisitCountSetting(Activity activity) {

        depth++;
        visitActivityStack.push(activity);

        for (SequenceFlow sequenceFlow : activity.getIncomingSequenceFlows()) {
            Activity sourceActivity = sequenceFlow.getSourceActivity();

            if (sourceActivity == null)
                continue;

            if (visitActivityStack.contains(sourceActivity)) { // there are some feedback activity!
                if (startEvent == null)
                    continue;
                for (Activity stackActivity : visitActivityStack) {

                    // find out which link is the feedback link
                    for (SequenceFlow sequenceFlowToSourceActivity : stackActivity.getOutgoingSequenceFlows()) {
                        if (sequenceFlowToSourceActivity.isFeedback())
                            continue;
                        if (getDepthFromStartEvent(
                                sequenceFlowToSourceActivity.getSourceActivity()) > getDepthFromStartEvent(
                                        sequenceFlowToSourceActivity.getTargetActivity())) {
                            sequenceFlowToSourceActivity.setFeedback(true); // mark as feedback link
                        }
                    }
                }

                continue; // cancel the already visited previous activity (feedback)
            }

            Integer visitCountForThisActivity = 0;
            if (visitCount.containsKey(sourceActivity.getTracingTag())) {
                visitCountForThisActivity = visitCount.get(sourceActivity.getTracingTag());
            }

            visitCountForThisActivity++;

            visitCount.put(sourceActivity.getTracingTag(), visitCountForThisActivity);

            visitForDepthAndVisitCountSetting(sourceActivity); // <-- point of recursive

            distancesByActivity.put(sourceActivity.getTracingTag(), depth);

        }

        depth--;
        visitActivityStack.pop();
    }

    public void visitToLineUp(Activity activity) {
        Set<Activity> visitedActivities = new HashSet<>();

        visitToLineUpInternal(activity, visitedActivities);
    }

    private void visitToLineUpInternal(Activity activity, Set<Activity> visitedActivities) {
        if (visitedActivities.contains(activity)) {
            return;
        }

        visitedActivities.add(activity);

        for (SequenceFlow sequenceFlow : activity.getIncomingSequenceFlows()) {
            if (sequenceFlow.isFeedback())
                continue;

            Activity sourceActivity = sequenceFlow.getSourceActivity();

            Integer distanceOfThis = distancesByActivity.get(sourceActivity.getTracingTag());

            if (!activitiesByDistanceMap.containsKey(distanceOfThis)) {
                activitiesByDistanceMap.put(distanceOfThis, new ArrayList<Activity>());
            }

            if (!activitiesByDistanceMap.get(distanceOfThis).contains(sourceActivity)) {
                activitiesByDistanceMap.get(distanceOfThis).add(sourceActivity);
            }

            visitToLineUpInternal(sourceActivity, visitedActivities);
        }
    }

    protected void findBlockMembers() {
        blockMembers = new ArrayList<Activity>();
        int branch = joinActivity.getIncomingSequenceFlows().size();

        for (int i = 1; activitiesByDistanceMap.containsKey(i) && activitiesByDistanceMap.get(i).size() > 0; i++) {

            List<Activity> activitiesInDepth = activitiesByDistanceMap.get(i);
            if (activitiesInDepth.size() == 1) {

                Activity activity = activitiesInDepth.get(0);
                blockMembers.add(activity);
                int visitedCountForThis = visitCount.get(activity.getTracingTag());

                if (branch == visitedCountForThis) { // stops when the number of splitted branch is same with visited
                                                     // count for the activity
                    break;
                }
            } else {
                for (Activity activity : activitiesInDepth) {
                    branch += activity.getIncomingSequenceFlows().size() - 1;

                    blockMembers.add(activity);
                }
            }

        }

    }

    public static List<Activity> getPossibleBlockMembers(List<Activity> blockMembers, ProcessInstance instance)
            throws Exception {
        Activity theLastBlockMember = blockMembers.get(blockMembers.size() - 1);
        List possibleBlockMembers = new ArrayList<Activity>();

        possibleBlockMembers.add(theLastBlockMember);

        BlockFinder.visitForPossibleNodes(theLastBlockMember, blockMembers, instance, possibleBlockMembers);

        return possibleBlockMembers;
    }

    private static void visitForPossibleNodes(Activity activity, List<Activity> blockMembers, ProcessInstance instance,
            List<Activity> possibleNodes) throws Exception {

        for (Activity next : activity.getPossibleNextActivities(instance, "")) {
            if (blockMembers.contains(next) && !possibleNodes.contains(next)) {
                possibleNodes.add(next);
                visitForPossibleNodes(next, blockMembers, instance, possibleNodes);
            }
        }
    }

    public static List<Activity> getBlockMembers(Activity joinActivity) {
        return new BlockFinder(joinActivity).getBlockMembers();
    }

}

package org.uengine.kernel.bpmn;

import org.uengine.kernel.Activity;
import org.uengine.kernel.ProcessInstance;

import java.util.*;

public class BlockFinder {

    Activity joinActivity;

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
        visitForDepthAndVisitCountSetting(joinActivity);
        visitToLineUp(joinActivity);
        findBlockMembers();
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

            if (sequenceFlow.isFeedback())
                continue;

            if (sourceActivity == null)
                continue;

            if (visitActivityStack.contains(sourceActivity)) { // there are some feedback activity!
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

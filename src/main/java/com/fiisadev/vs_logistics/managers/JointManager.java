package com.fiisadev.vs_logistics.managers;

import org.valkyrienskies.core.api.events.PhysTickEvent;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.world.VsiPhysLevel;

import java.util.*;

public class JointManager {

    private static Map<Long, Set<Integer>> jointsByShip = Map.of();
    private static Map<Integer, long[]> shipsByJoint = Map.of();

    public static void onPhysicsTick(PhysTickEvent event) {
        VsiPhysLevel physLevel = (VsiPhysLevel) event.getWorld();

        Map<Long, Set<Integer>> snapshot = physLevel.getJointsByShipIds();
        if (snapshot.isEmpty()) {
            return;
        }

        jointsByShip = snapshot;

        Map<Integer, long[]> jointMap = new HashMap<>();

        for (Set<Integer> jointIds : snapshot.values()) {
            for (int jointId : jointIds) {
                if (jointMap.containsKey(jointId)) continue;

                VSJoint joint = physLevel.getJointById(jointId);
                if (joint == null) continue;

                Long a = joint.getShipId0();
                Long b = joint.getShipId1();
                if (a == null || b == null) continue;

                jointMap.put(jointId, new long[]{a, b});
            }
        }

        shipsByJoint = jointMap;
    }


    public static boolean isShipConnectedToShip(Ship target, Ship current) {
        return isShipConnectedToShip(target.getId(), current.getId(), new HashSet<>());
    }

    private static boolean isShipConnectedToShip(
            long targetId,
            long currentId,
            Set<Long> visitedShips
    ) {
        if (!visitedShips.add(currentId)) {
            return false;
        }

        if (currentId == targetId) {
            return true;
        }

        Set<Integer> jointIds = jointsByShip.get(currentId);
        if (jointIds == null || jointIds.isEmpty()) {
            return false;
        }

        for (int jointId : jointIds) {
            long[] ships = shipsByJoint.get(jointId);
            if (ships == null) continue;

            long otherShipId =
                    ships[0] == currentId ? ships[1] :
                            ships[1] == currentId ? ships[0] :
                                    -1;

            if (otherShipId == -1) continue;

            if (isShipConnectedToShip(targetId, otherShipId, visitedShips)) {
                return true;
            }
        }

        return false;
    }
}

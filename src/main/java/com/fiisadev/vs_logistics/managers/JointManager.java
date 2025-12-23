package com.fiisadev.vs_logistics.managers;

import org.valkyrienskies.core.api.events.PhysTickEvent;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.world.VsiPhysLevel;

import java.util.HashSet;
import java.util.Set;

public class JointManager {
    private static VsiPhysLevel physLevel;

    public static void onPhysicsTick(PhysTickEvent event) {
        if (physLevel == null)
            physLevel = (VsiPhysLevel)event.getWorld();
    }

    public static boolean isShipConnectedToShip(Ship target, Ship current) {
        return isShipConnectedToShip(target, current, new HashSet<>());
    }

    private static boolean isShipConnectedToShip(Ship target, Ship current, Set<Long> visitedShips) {
        if (visitedShips.contains(current.getId()))
            return false;

        visitedShips.add(current.getId());

        if (current.getId() == target.getId()) {
            return true;
        }

        for (int jointId : physLevel.getJointsFromShip(current.getId())) {
            VSJoint joint = physLevel.getJointById(jointId);
            if (joint == null) continue;

            Ship otherShip = physLevel.getShipById((joint.getShipId0() == current.getId()) ? joint.getShipId1() : joint.getShipId0());
            if (otherShip == null) continue;

            if (isShipConnectedToShip(target, otherShip, visitedShips)) {
                return true;
            }
        }

        return false;
    }

}

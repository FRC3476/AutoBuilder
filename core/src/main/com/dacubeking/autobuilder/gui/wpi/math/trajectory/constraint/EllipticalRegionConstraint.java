// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package com.dacubeking.autobuilder.gui.wpi.math.trajectory.constraint;

import com.dacubeking.autobuilder.gui.wpi.math.geometry.Pose2d;
import com.dacubeking.autobuilder.gui.wpi.math.geometry.Rotation2d;
import com.dacubeking.autobuilder.gui.wpi.math.geometry.Translation2d;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Enforces a particular constraint only within an elliptical region. */
public class EllipticalRegionConstraint implements TrajectoryConstraint {
  @JsonProperty("center") private final Translation2d m_center;
  @JsonProperty("radii") private final Translation2d m_radii;
  @JsonProperty("constraint") private final TrajectoryConstraint m_constraint;

  /**
   * Constructs a new EllipticalRegionConstraint.
   *
   * @param center The center of the ellipse in which to enforce the constraint.
   * @param xWidth The width of the ellipse in which to enforce the constraint.
   * @param yWidth The height of the ellipse in which to enforce the constraint.
   * @param rotation The rotation to apply to all radii around the origin.
   * @param constraint The constraint to enforce when the robot is within the region.
   */
  @SuppressWarnings("ParameterName")
  public EllipticalRegionConstraint(
      Translation2d center,
      double xWidth,
      double yWidth,
      Rotation2d rotation,
      TrajectoryConstraint constraint) {
    m_center = center;
    m_radii = new Translation2d(xWidth / 2.0, yWidth / 2.0).rotateBy(rotation);
    m_constraint = constraint;
  }

  @JsonCreator
  public EllipticalRegionConstraint(@JsonProperty("center") Translation2d center,
                                    @JsonProperty("radii") Translation2d radii,
                                    @JsonProperty("constraint") TrajectoryConstraint constraint) {
    m_center = center;
    m_radii = radii;
    m_constraint = constraint;
  }

  @Override
  public double getMaxVelocityMetersPerSecond(Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond) {
    if (isPoseInRegion(poseMeters)) {
      return m_constraint.getMaxVelocityMetersPerSecond(
              poseMeters, curvatureRadPerMeter, velocityMetersPerSecond);
    } else {
      return Double.POSITIVE_INFINITY;
    }
  }



  @Override
  public MinMax getMinMaxAccelerationMetersPerSecondSq(
      Pose2d poseMeters, double curvatureRadPerMeter, double velocityMetersPerSecond) {
    if (isPoseInRegion(poseMeters)) {
      return m_constraint.getMinMaxAccelerationMetersPerSecondSq(
          poseMeters, curvatureRadPerMeter, velocityMetersPerSecond);
    } else {
      return new MinMax();
    }
  }

  /**
   * Returns whether the specified robot pose is within the region that the constraint is enforced
   * in.
   *
   * @param robotPose The robot pose.
   * @return Whether the robot pose is within the constraint region.
   */
  public boolean isPoseInRegion(Pose2d robotPose) {
    // The region (disk) bounded by the ellipse is given by the equation:
    // ((x-h)^2)/Rx^2) + ((y-k)^2)/Ry^2) <= 1
    // If the inequality is satisfied, then it is inside the ellipse; otherwise
    // it is outside the ellipse.
    // Both sides have been multiplied by Rx^2 * Ry^2 for efficiency reasons.
    return Math.pow(robotPose.getX() - m_center.getX(), 2) * Math.pow(m_radii.getY(), 2)
            + Math.pow(robotPose.getY() - m_center.getY(), 2) * Math.pow(m_radii.getX(), 2)
        <= Math.pow(m_radii.getX(), 2) * Math.pow(m_radii.getY(), 2);
  }
}

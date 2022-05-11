// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package me.varun.autobuilder.wpi.math.estimator;

import me.varun.autobuilder.wpi.math.Matrix;
import me.varun.autobuilder.wpi.math.Num;
import me.varun.autobuilder.wpi.math.numbers.N1;

@SuppressWarnings({"ParameterName", "InterfaceTypeParameterName"})
interface KalmanTypeFilter<States extends Num, Inputs extends Num, Outputs extends Num> {
  Matrix<States, States> getP();

  double getP(int i, int j);

  void setP(Matrix<States, States> newP);

  Matrix<States, N1> getXhat();

  double getXhat(int i);

  void setXhat(Matrix<States, N1> xHat);

  void setXhat(int i, double value);

  void reset();

  void predict(Matrix<Inputs, N1> u, double dtSeconds);

  void correct(Matrix<Inputs, N1> u, Matrix<Outputs, N1> y);
}
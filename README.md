OOP-Proj3
==========

Chung-Ang Univercity. Objected-Oriented Programming Project3 - Graphics Game 'ARKANOID'

Student name & ID
================
Name:¼­±Ô¹Î 
Id:20225679

Overview
=========

This program is Graphics Game 'ARKANOID' that implements a simple 3D virtual billiard game using Direct3D. 
The game consists of a playing field, walls, multiple balls, a control ball, and a bullet ball. 
The player can control the position of the control ball and shoot the bullet ball to collide with other balls.

Contents
=========
virtualLego.cpp: Main source code file containing the implementation of the virtual billiard game.
d3dUtility.h: Header file containing utility functions for Direct3D initialization and message loop.
d3dUtility.cpp: Source file implementing the utility functions for Direct3D.

How to compile
----------------
OS : Windows 10     
IDE : Visual Studio 2022

How to Build and Run
====================
1.Open the provided Visual Studio project file or create a new project.
2.Ensure that the necessary Direct3D libraries and dependencies are properly configured in the project settings.
3.Build the project.
4.Run the executable generated after the build.

Controls
=========
Spacebar: Shoot the bullet ball. Pressing spacebar again will respawn the bullet ball at the initial position.
Left Arrow: Move the control ball to the left within the limits of the playing field.
Right Arrow: Move the control ball to the right within the limits of the playing field.
R Key: Restart the game. This resets the positions of all balls and restores the initial number of lives.
P Key: Pause/Unpause the game. When paused, the bullet ball's velocity is set to zero.
Mouse Movement: Move the control ball horizontally by moving the mouse left or right.

Game Rules
===========
The goal of the game is to hit and clear all of the other balls with the bullet ball.
Each time the bullet ball reaches the bottom of the playing field, it respawns and deducts one life each have five lifes.
The game ends when all lives are exhausted.


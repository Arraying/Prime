# Prime
Prime -- Nashorn Engine on steroids

## What it does
Prime allows custom imports within Nashorn, completely customizable. Prime also features a class filter.

## What it needs
* Execution time limit
* Execution CPU limit
* Execution memory limit
* Optimization

## Usage

1) Add the project as a dependency via [JitPack](https://jitpack.io/#arraying/Prime).
2) Create a new `Prime.Builder` object.
3) Add desired source providers.
4) Build the builder to create a `Prime` object.
5) Run `Prime#evaluate`.

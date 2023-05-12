package com.dddeurope.recycle.domain;

public enum Location {
    DEFAULT {
        @Override
        double calculatePriceFor(String fractionType, int weight) {
            return weight * priceFor(fractionType);
        }

        private double priceFor(String fractionType) {
            return switch (fractionType) {
                case "Construction waste" -> 0.15;
                case "Green waste" -> 0.09;
                default -> throw new IllegalArgumentException("Unknown fraction " + fractionType);
            };
        }
    },
    SOUTH_PARK {
        @Override
        public double calculatePriceFor(String fractionType, int weight) {
            return chargedWeightFor(fractionType, weight) * priceFor(fractionType);
        }

        private int chargedWeightFor(String fractionType, int weight) {
            return switch (fractionType) {
                case "Construction waste" -> Math.max(0, weight - 100);
                case "Green waste" -> Math.max(0, weight - 50);
                default -> throw new IllegalArgumentException("Unknown fraction " + fractionType);
            };
        }

        private double priceFor(String fractionType) {
            return switch (fractionType) {
                case "Construction waste" -> 0.18;
                case "Green waste" -> 0.12;
                default -> throw new IllegalArgumentException("Unknown fraction " + fractionType);
            };
        }
    };

    abstract double calculatePriceFor(String fractionType, int weight);
}

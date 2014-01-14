package team204.util;

import battlecode.common.Direction;

public enum Rotation {
    LEFT {
        @Override
        public Direction rotate(Direction dir) {
            return dir.rotateLeft();
        }
        
        @Override
        public Rotation opposite() {
            return RIGHT;
        }
        
        @Override
        public int difference(Direction dir1, Direction dir2) {
            if (dir1 == Direction.OMNI || dir2 == Direction.OMNI) {
                return 0;
            }
            return (dir1.ordinal() - dir2.ordinal() + 8) % 8;
        }
    },
    RIGHT {
        @Override
        public Direction rotate(Direction dir) {
            return dir.rotateRight();
        }
        
        @Override
        public Rotation opposite() {
            return LEFT;
        }
        
        @Override
        public int difference(Direction dir1, Direction dir2) {
            if (dir1 == Direction.OMNI || dir2 == Direction.OMNI) {
                return 0;
            }
            return (dir2.ordinal() - dir1.ordinal() + 8) % 8;
        }
    },
    EITHER {
        @Override
        public Direction rotate(Direction dir) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Rotation opposite() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public int difference(Direction dir1, Direction dir2) {
            return Math.min(LEFT.difference(dir1, dir2), RIGHT.difference(dir1, dir2));
        }
        
        @Override
        public Rotation toLeftOrRight() {
            return Rotation.LEFT;
        }
    },
    ;
    
    public abstract Direction rotate(Direction dir);
    
    public abstract Rotation opposite();
    
    /**
     * Calculates the number of rotations which must be made in order to reach the second direction from the first
     * 
     * @param dir1
     *            the non-null, non-NONE, non-OMNI start direction
     * @param dir2
     *            the non-null, non-NONE, non-OMNI end direction
     * @return the number of rotations separating dir2 from dir1
     */
    public abstract int difference(Direction dir1, Direction dir2);
    
    public static Rotation shortestTo(Direction dir1, Direction dir2) {
        int diffLeft = Rotation.LEFT.difference(dir1, dir2);
        int diffRight = Rotation.RIGHT.difference(dir1, dir2);
        if (diffLeft < diffRight) {
            return Rotation.LEFT;
        } else if (diffLeft > diffRight) {
            return Rotation.RIGHT;
        } else {
            return Rotation.EITHER;
        }
    }
    
    public Rotation toLeftOrRight() {
        return this;
    }
}

package assignment.utility;

import java.io.Serializable;

public class Pair<X, Y> implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final int PRIME = 31;
	private final X x;
	private final Y y;

	public Pair(final X el1, final Y el2) {
		super();
		this.x = el1;
		this.y = el2;
	}

	public X getX() {
		return x;
	}

	public Y getY() {
		return y;
	}

	public int hashCode() {
		int result = 1;
		result = PRIME * result + ((x == null) ? 0 : x.hashCode());
		result = PRIME * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}

	@SuppressWarnings("rawtypes")
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Pair other = (Pair) obj;
		if (x == null) {
			if (other.x != null) {
				return false;
			}
		} else if (!x.equals(other.x)) {
			return false;
		}
		if (y == null) {
			if (other.y != null) {
				return false;
			}
		} else if (!y.equals(other.y)) {
			return false;
		}
		return true;
	}

	public String toString() {
		return "Pair [x=" + x + ", y=" + y + "]";
	}

}
package com.oddsoft.tpetrash2;

public class TrashItem implements Comparable<TrashItem> {

	private String location;
	private String time;
	private String name;

	public TrashItem(String location, String time, String name) {
		this.location = location;
		this.time = time;
		this.name = name;
	}
	
	@Override
	public String toString() {

		String result = getTime() + " " + getName();
		return result;
	}
    @Override
    public int compareTo(TrashItem fi) {
        return this.toString().compareToIgnoreCase(fi.toString());
    }
    
	public String getName() {
		return name;
	}
	public String getLocation() {
		return location;
	}
	public String getTime() {
		return time;
	}

	public String getItemLabel() {
		return time + " " + name;
	}
}
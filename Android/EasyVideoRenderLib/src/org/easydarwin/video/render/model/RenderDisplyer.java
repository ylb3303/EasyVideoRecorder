package org.easydarwin.video.render.model;

public class RenderDisplyer {
	private String type;
	private int order;
	private int action;
	private String id;
	private String name;
	private String icon;
	private String location;
	private boolean enable;

	public RenderDisplyer() {
	}

	public String getType() {
		return type;
	}

	public RenderDisplyer setType(String type) {
		this.type = type;
		return this;
	}

	public String getId() {
		return id;
	}

	public RenderDisplyer setId(String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public RenderDisplyer setName(String name) {
		this.name = name;
		return this;
	}

	public String getIcon() {
		return icon;
	}

	public RenderDisplyer setIcon(String icon) {
		this.icon = icon;
		return this;
	}

	public boolean isEnable() {
		return enable;
	}

	public RenderDisplyer setEnable(boolean enable) {
		this.enable = enable;
		return this;
	}

	public String getLocation() {
		return location;
	}

	public RenderDisplyer setLocation(String location) {
		this.location = location;
		return this;
	}

	public int getOrder() {
		return order;
	}

	public RenderDisplyer setOrder(int order) {
		this.order = order;
		return this;
	}

	public int getAction() {
		return action;
	}

	public RenderDisplyer setAction(int action) {
		this.action = action;
		return this;
	}

}

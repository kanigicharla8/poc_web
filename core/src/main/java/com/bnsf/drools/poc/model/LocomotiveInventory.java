package com.bnsf.drools.poc.model;

import com.google.common.base.Objects;

/**
 * 
 * @author rakesh
 *
 */
public class LocomotiveInventory extends AbstractBNSFModel<String>{

	private String locomotiveId;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LocomotiveInventory)) return false;
		LocomotiveInventory that = (LocomotiveInventory) o;
		return Objects.equal(locomotiveId, that.locomotiveId) &&
				Objects.equal(trainId, that.trainId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(locomotiveId,trainId);
	}

	/**
	 * @see com.bnsf.drools.poc.model.BNSFModel#getId()
	 */
	public String getId() {
		return getLocomotiveId();
	}
	
	/*
	 * getters and setters
	 */
	public String getLocomotiveId() {
		return locomotiveId;
	}

	public void setLocomotiveId(String locomotiveId) {
		this.locomotiveId = locomotiveId;
	}

}

package com.meral.restaws;

import java.util.Date;

public class NameDateIpUaId {
	private String myname;
	private Date mydate;
	private String myIP;
	private String myUserAgent;
	private String myGUID;
	public NameDateIpUaId(String myname, Date mydate, String myIP,
			String userAgent) {
		super();
		this.myname = myname;
		this.mydate = mydate;
		this.myIP = myIP;
		this.myUserAgent = userAgent;
		this.myGUID = java.util.UUID.randomUUID().toString();
	}
	@Override
	public String toString() {
		return "NameDateIpUaId [myname=" + myname + ", mydate=" + mydate
				+ ", myIP=" + myIP + ", userAgent=" + myUserAgent + ", guid="
				+ myGUID + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myGUID == null) ? 0 : myGUID.hashCode());
		result = prime * result + ((myIP == null) ? 0 : myIP.hashCode());
		result = prime * result + ((mydate == null) ? 0 : mydate.hashCode());
		result = prime * result + ((myname == null) ? 0 : myname.hashCode());
		result = prime * result
				+ ((myUserAgent == null) ? 0 : myUserAgent.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NameDateIpUaId other = (NameDateIpUaId) obj;
		if (myGUID == null) {
			if (other.myGUID != null)
				return false;
		} else if (!myGUID.equals(other.myGUID))
			return false;
		if (myIP == null) {
			if (other.myIP != null)
				return false;
		} else if (!myIP.equals(other.myIP))
			return false;
		if (mydate == null) {
			if (other.mydate != null)
				return false;
		} else if (!mydate.equals(other.mydate))
			return false;
		if (myname == null) {
			if (other.myname != null)
				return false;
		} else if (!myname.equals(other.myname))
			return false;
		if (myUserAgent == null) {
			if (other.myUserAgent != null)
				return false;
		} else if (!myUserAgent.equals(other.myUserAgent))
			return false;
		return true;
	}
	public String getMyname() {
		return myname;
	}
	public void setMyname(String myname) {
		this.myname = myname;
	}
	public Date getMydate() {
		return mydate;
	}
	public void setMydate(Date mydate) {
		this.mydate = mydate;
	}
	public String getMyIP() {
		return myIP;
	}
	public void setMyIP(String myIP) {
		this.myIP = myIP;
	}
	public String getUserAgent() {
		return myUserAgent;
	}
	public void setUserAgent(String userAgent) {
		this.myUserAgent = userAgent;
	}
	public String getGuid() {
		return myGUID;
	}
	public void setGuid(String guid) {
		this.myGUID = guid;
	}
}

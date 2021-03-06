package gov.usgs.volcanoes.swarm.data.seedlink;

import gov.usgs.volcanoes.core.xml.SimpleXmlParser;
import gov.usgs.volcanoes.core.xml.XmlDocHandler;
import gov.usgs.volcanoes.swarm.AbstractChannelInfo;
import gov.usgs.volcanoes.swarm.ChannelUtil;
import gov.usgs.volcanoes.swarm.GroupsType;
import gov.usgs.volcanoes.swarm.data.SeismicDataSource;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SeedLink channel information.
 * 
 * @author Kevin Frechette (ISTI)
 */
public class SeedLinkChannelInfo extends AbstractChannelInfo {
  /**
   * SeedLink XML document handler.
   */
  protected class SeedLinkStationXmlDocHandler implements XmlDocHandler {
    private void clearChannel() {
      channel = null;
      location = null;
      type = null;
    }

    private void clearStation() {
      station = null;
      network = null;
      clearChannel();
    }

    /**
     * Document ended.
     * 
     * @throws Exception when things go wrong
     */
    public void endDocument() throws Exception {}

    /**
     * Element ended.
     * 
     * @param tag Tag name
     * @throws Exception when things go wrong
     */
    public void endElement(String tag) throws Exception {}

    /**
     * Document started.
     * 
     * @throws Exception when things go wrong
     */
    public void startDocument() throws Exception {}

    /**
     * Element started.
     * 
     * @param tag Tag name
     * @param h map of tag attributes and values
     * @throws Exception when things go wrong
     */
    public void startElement(String tag, Map<String, String> h)
        throws Exception {
      if ("station".equals(tag)) {
        clearStation();
        for (String key : h.keySet()) {
          String val = h.get(key);
          if ("name".equals(key)) {
            station = val;
          } else if ("network".equals(key)) {
            network = val;
          }
        }
      } else if ("stream".equals(tag)) {
        clearChannel();
        for (String key : h.keySet()) {
          String val = h.get(key);
          if ("seedname".equals(key)) {
            channel = val;
          } else if ("location".equals(key)) {
            location = val;
          } else if ("type".equals(key)) {
            type = val;
          }
        }
        if (station != null && network != null && channel != null
            && location != null && DATA_TYPE.equals(type)) {
          ChannelUtil.addChannel(channels, SeedLinkChannelInfo.this, getSource());
        }
      }
    }

    /**
     * Text or CDATA found.
     * 
     * @param str string
     * @throws Exception when things go wrong
     */
    public void text(String str) throws Exception {}
  }

  /** Data type. */
  public static final String DATA_TYPE = "D";

  private final List<String> channels = new ArrayList<String>();

  /** The data source. */
  private final SeedLinkSource dataSource;


  private double latitude = Double.NaN;
  private double longitude = Double.NaN;

  private String location;
  private String network;
  private String channel;
  private String station;
  private String type;

  /**
   * Constructor.
   * 
   * @param dataSource data source
   * @param infoStr info string
   * @throws Exception when things go wrong
   */
  public SeedLinkChannelInfo(SeedLinkSource dataSource, String infoStr) throws Exception {
    this.dataSource = dataSource;
    Reader reader = new StringReader(infoStr);
    SimpleXmlParser.parse(new SeedLinkStationXmlDocHandler(), reader);
  }

  /**
   * Get the channel name.
   * 
   * @return the channel name.
   */
  public String getChannel() {
    return channel;
  }

  /**
   * Get the channels.
   * 
   * @return the list of channels.
   */
  public List<String> getChannels() {
    return channels;
  }

  /**
   * Get the groups.
   * 
   * @return the list of groups.
   */
  public List<String> getGroups() {
    return ChannelUtil.getGroups(this, GroupsType.NETWORK_AND_SITE);
  }

  /**
   * Get the latitude.
   * 
   * @return the latitude.
   */
  public double getLatitude() {
    return latitude;
  }

  /**
   * Get the location.
   * 
   * @return the location.
   */
  public String getLocation() {
    return location;
  }

  /**
   * Get the longitude.
   * 
   * @return the longitude.
   */
  public double getLongitude() {
    return longitude;
  }

  /**
   * Get the network name.
   * 
   * @return the network name.
   */
  public String getNetwork() {
    return network;
  }

  /**
   * Get the site name.
   * A noop since siteName is not set.
   * 
   * @return the site name.
   */
  public String getSiteName() {
    return null;
  }

  /**
   * Get the seismic data source.
   * 
   * @return the seismic data source.
   */
  public SeismicDataSource getSource() {
    return dataSource;
  }

  /**
   * Get the station name.
   * 
   * @return the station name.
   */
  public String getStation() {
    return station;
  }

  /**
   * Get the type.
   * 
   * @return the type.
   */
  public String getType() {
    return type;
  }
}

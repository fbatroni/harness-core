package software.wings.service.intfc;

import software.wings.beans.*;

public interface InfraService {
  PageResponse<Infra> listInfra(String envID, PageRequest<Infra> pageRequest);
  public Infra createInfra(Infra infra, String envID);

  public PageResponse<Host> listHosts(PageRequest<Host> req);
  public Host getHost(String infraID, String hostID);
  public Host createHost(String infraID, Host host);
  public Host updateHost(String infraID, Host host);

  public Tag createTag(String envID, Tag tag);

  public Host applyTag(String hostID, String tagID);
}

package cn.civilizationdata.shiro.casbin.properties;

import lombok.Data;

@Data
public class CasbinProperties {
    private String etcd;

    private String etcdKey = "/casbin/watcher_path";

    private String model = "classpath:casbin/model.conf";

    private String dbtable = "casbin_rule_path";
}

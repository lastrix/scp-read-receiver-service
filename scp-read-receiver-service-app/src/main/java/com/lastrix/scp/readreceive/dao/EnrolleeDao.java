package com.lastrix.scp.readreceive.dao;


import com.lastrix.scp.writesender.model.EnrolleeSelect;
import com.lastrix.scp.writesender.model.EnrolleeSelectId;

import java.util.List;

public interface EnrolleeDao {
    int commit(List<EnrolleeSelectId> changes);

    List<EnrolleeSelectId> fetch(int page);

    int update(List<EnrolleeSelect> list);
}

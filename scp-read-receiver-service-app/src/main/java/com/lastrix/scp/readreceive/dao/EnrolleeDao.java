package com.lastrix.scp.readreceive.dao;

import com.lastrix.scp.readreceive.model.EnrolleeSelectId;

import java.util.List;

public interface EnrolleeDao {
    int commit(List<EnrolleeSelectId> changes);

    List<EnrolleeSelectId> fetch(int page);
}

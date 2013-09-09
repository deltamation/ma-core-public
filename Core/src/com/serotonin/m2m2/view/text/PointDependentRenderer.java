/*
 * Copyright (C) 2013 Deltamation Software. All rights reserved.
 * @author Jared Wiltshire
 */

package com.serotonin.m2m2.view.text;

import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Copyright (C) 2013 Deltamation Software. All rights reserved.
 * @author Jared Wiltshire
 */
public interface PointDependentRenderer {
    public abstract DataPointVO getPoint();
    public abstract void setPoint(DataPointVO point);
}

/*
 * Copyright (C) 2013 Deltamation Software. All rights reserved.
 * @author Jared Wiltshire
 */

package com.serotonin.m2m2.db.upgrade;

/**
 * Copyright (C) 2013 Deltamation Software. All rights reserved.
 * @author Jared Wiltshire
 */
public class Upgrade7 extends DBUpgrade {
    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.upgrade.DBUpgrade#upgrade()
     */
    @Override
    protected void upgrade() throws Exception {
        ejt.update("ALTER TABLE pointvalues ADD COLUMN modifiedValue double");
    }

    /* (non-Javadoc)
     * @see com.serotonin.m2m2.db.upgrade.DBUpgrade#getNewSchemaVersion()
     */
    @Override
    protected String getNewSchemaVersion() {
        return "8";
    }
}

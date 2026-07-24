package com.api.bugzapper.constant;

/**
 * Fixed role_id values seeded by docs/db/schema-v2-role-model.sql.
 * MyBatis @Select/@Insert/@Update SQL strings can't reference these constants
 * directly (annotation values must be compile-time constants embedded in the
 * string), so raw literals still appear in repository SQL — keep them in sync
 * with this table and comment each one with "role_id N = NAME".
 */
public final class RoleId {
    private RoleId() {
    }

    public static final int COMPANY_OWNER = 1;
    public static final int PROJECT_MANAGER = 2;
    public static final int PHASE_LEAD = 3;
    public static final int DEVELOPER = 4;
    public static final int RECRUITER = 5;
    public static final int BUG_HUNTER = 6;
}

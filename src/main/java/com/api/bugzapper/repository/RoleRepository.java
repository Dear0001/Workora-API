package com.api.bugzapper.repository;

import com.api.bugzapper.model.entity.Role;
import com.api.bugzapper.model.request.RoleRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleRepository {

    @Select("""
       INSERT INTO roles (role_name) VALUES (#{roleName.roleName})
       RETURNING role_name
       """)
    @Results(id = "roleResult", value ={
            @Result(property = "roleId", column = "role_id"),
            @Result(property = "roleName", column = "role_name")
    })
    Role createRole(@Param("roleName") RoleRequest roleRequest);

    @Select("""
        SELECT * FROM roles ORDER BY role_id ASC
        LIMIT #{size} OFFSET #{size} * (#{page} - 1)
       """)
    @ResultMap("roleResult")
    List<Role> getAllRoles(Integer page, Integer size);

    @Delete("""
        DELETE FROM roles WHERE role_id = #{id}
       """)
    void deleteRole(Integer id);


    @Select("""
       SELECT * FROM roles WHERE role_id = #{id}
       """)
    @ResultMap("roleResult")
    Role getRoleById(Integer id);

    @Update("""
        UPDATE roles SET role_name = #{roleRequest.roleName} WHERE role_id = #{id}
       """)
    @ResultMap("roleResult")
    void updateRole(Integer id,@Param("roleRequest") RoleRequest roleRequest);

    @Select("""
    SELECT COUNT(*)
    FROM roles
    WHERE role_id = #{roleId}
    """)
    boolean roleExists(@Param("roleId") Integer roleId);
}

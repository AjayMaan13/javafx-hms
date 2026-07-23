package com.hotel.controller.admin;

/**
 * Optional hook for admin screens loaded into AdminShell's content area. Screens that need
 * to navigate elsewhere, read the logged-in admin, or read shell-held selection state
 * (e.g. "which reservation was opened from the Dashboard") implement this.
 */
public interface AdminScreenController {

    void setShell(AdminShellController shell);
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.Dataset;
import edu.harvard.iq.dataverse.IdServiceBean;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.engine.command.AbstractVoidCommand;
import edu.harvard.iq.dataverse.engine.command.CommandContext;
import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import edu.harvard.iq.dataverse.engine.command.RequiredPermissions;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.engine.command.exception.PermissionException;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author skraffmi
 */
@RequiredPermissions(Permission.EditDataset)
public class UpdateDatasetTargetURLCommand extends AbstractVoidCommand  {

    private final Dataset target;
    
    public UpdateDatasetTargetURLCommand( Dataset target, DataverseRequest aRequest) {
        super(aRequest, target);
        this.target = target;
    }

    @Override
    protected void executeImpl(CommandContext ctxt) throws CommandException {

        if (!(getUser() instanceof AuthenticatedUser) || !getUser().isSuperuser()) {
            throw new PermissionException("Update Target URL can only be called by superusers.",
                    this, Collections.singleton(Permission.EditDataset), target);
        }

        IdServiceBean idServiceBean = IdServiceBean.getBean(target.getProtocol(), ctxt);
        HashMap<String, String> metadata = idServiceBean.getMetadataFromDatasetForTargetURL(target);
        try {
            String doiRetString = idServiceBean.modifyIdentifier(target, metadata);
            if (doiRetString != null && doiRetString.contains(target.getIdentifier())) {
                target.setGlobalIdCreateTime(new Timestamp(new Date().getTime()));
                ctxt.em().merge(target);
                ctxt.em().flush();
            } else {
                //do nothing - we'll know it failed because the global id create time won't have been updated.
            }
        }catch (Exception e) {
            //do nothing - idem and the problem has been logged
        }
    }
    
}

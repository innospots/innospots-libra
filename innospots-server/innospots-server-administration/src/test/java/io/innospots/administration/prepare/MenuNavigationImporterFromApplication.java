package io.innospots.administration.prepare;

import io.innospots.libra.base.extension.LibraClassPathExtPropertiesLoader;
import io.innospots.libra.base.extension.LibraExtensionProperties;
import io.innospots.libra.base.menu.BaseItem;
import io.innospots.libra.base.menu.ItemType;
import io.innospots.libra.base.menu.OptElement;
import io.innospots.libra.base.menu.ResourceItem;
import io.innospots.libra.kernel.LibraKernelImporter;
import io.innospots.libra.kernel.module.menu.dao.MenuResourceDao;
import io.innospots.libra.kernel.module.menu.entity.MenuResourceEntity;
import io.innospots.libra.kernel.module.menu.converter.MenuResourceConverter;
import io.innospots.libra.kernel.module.menu.operator.MenuManagementOperator;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import java.util.List;

/**
 * import application menus into system directly.
 *
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/22
 */
@SpringBootTest
@LibraKernelImporter
@ActiveProfiles("local")
//@Import(InnospotAdministrationServer.class)
public class MenuNavigationImporterFromApplication {

    @Autowired
    private MenuManagementOperator menuManagementOperator;

    @Autowired
    private MenuResourceDao menuResourceDao;

    @Test
    void menuImport() {
        Assert.notNull(menuManagementOperator, "");
        List<LibraExtensionProperties> libraAppProperties = LibraClassPathExtPropertiesLoader.loadFromClassPath();

        for (LibraExtensionProperties libraAppProperty : libraAppProperties) {
            for (ResourceItem menu : libraAppProperty.getModules()) {
                insert(menu, null);
            }
        }
    }

    private void insert(ResourceItem resourceItem, MenuResourceEntity parentItem) {

        MenuResourceEntity menuResourceEntity = newMenuItem(resourceItem, parentItem);
        if (menuResourceEntity != null) {
            menuResourceEntity.setItemType(ItemType.MENU);
            int p = menuResourceDao.insert(menuResourceEntity);
        }

        if (CollectionUtils.isNotEmpty(resourceItem.getItems())) {
            for (ResourceItem item : resourceItem.getItems()) {
                item.setItemType(ItemType.MENU);
                insert(item, menuResourceEntity);
            }
        }

        if (CollectionUtils.isNotEmpty(resourceItem.getOpts())) {
            for (OptElement opt : resourceItem.getOpts()) {
                MenuResourceEntity newMenuItem = newMenuItem(opt, menuResourceEntity);
                menuResourceDao.insert(newMenuItem);
            }
        }

    }

    private MenuResourceEntity newMenuItem(OptElement optElement, MenuResourceEntity parentItem) {
        MenuResourceEntity newMenuItem = MenuResourceConverter.INSTANCE.optItemEntity(optElement);
        newMenuItem.setParentId(parentItem.getResourceId());
        newMenuItem.setMenuGroup(parentItem.getMenuGroup());
        newMenuItem.setShowMenu(false);
        newMenuItem.setStatus(true);
        newMenuItem.setItemKey(parentItem.getItemKey() + optElement.getItemKey());
        newMenuItem.setOpenMode(BaseItem.OpenMode.INTERNAL);
        System.out.println(newMenuItem);
        return newMenuItem;
    }

    private MenuResourceEntity newMenuItem(ResourceItem resourceItem, MenuResourceEntity parentItem) {
        MenuResourceEntity newMenuItem = MenuResourceConverter.INSTANCE.menuItemToEntity(resourceItem);
        newMenuItem.setOpenMode(BaseItem.OpenMode.INTERNAL);
        newMenuItem.setStatus(true);
        newMenuItem.setItemType(ItemType.MENU);
        if (parentItem != null) {
            newMenuItem.setParentId(parentItem.getResourceId());
            newMenuItem.setMenuGroup(parentItem.getMenuGroup());
        } else {
            newMenuItem.setMenuGroup("system");
        }
        System.out.println(newMenuItem);
        return newMenuItem;
    }

}

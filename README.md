# iDempiere Tenant Delete/Initialize Plugin

iDempiere 多租戶管理插件，提供 System Admin 層級的 Tenant 初始化與刪除功能。

## 功能說明

| Process | 功能 | 說明 |
|---------|------|------|
| **Initialize Client** | 初始化交易資料 | 刪除交易資料（訂單、發票、付款等），保留主資料（產品、客戶、會計科目等） |
| **Delete Client (Tenant)** | 刪除整個 Tenant | 完全刪除一個 Tenant 的所有資料，包括主資料和 Client 本身 |
| **Initialize Client Preview** | 預覽初始化 | 顯示將被刪除的記錄數統計，不實際執行 |

## 安裝步驟

### 1. 編譯插件

```bash
cd tw.idempiere.clientinit
mvn clean package -DskipTests
```

### 2. 部署 JAR

複製 `target/tw.idempiere.clientinit-12.0.0-SNAPSHOT.jar` 到 iDempiere 的 `plugins/` 目錄。

### 3. 執行 PackIn

1. 以 **System Admin** 身份登入 iDempiere（Client = System）
2. 進入 **System Admin > Pack In**
3. 選擇檔案：`2pack/202603210112_System_ClientInitProcess.zip`
4. 執行 PackIn

### 4. 建立選單 (Menu)

PackIn 只會建立 Process，需要手動建立選單：

1. 進入 **System Admin > General Rules > System Rules > Menu**
2. 建立新選單項目：

| 欄位 | Initialize Client | Delete Client |
|------|-------------------|---------------|
| Name | Initialize Client | Delete Client (Tenant) |
| Action | Process | Process |
| Process | Initialize Client | Delete Client (Tenant) |
| Summary Level | No | No |

3. 將選單項目加入適當的選單樹（例如：System Admin）

## 操作說明

### 初始化 Tenant（保留主資料）

適用場景：清除測試交易資料，重新開始營運

1. 以 **System Admin** 登入（Client = System）
2. 執行 **Initialize Client** Process
3. 選擇 **Target Client**（要初始化的 Tenant）
4. 可選：設定日期範圍（只刪除指定日期範圍內的交易）
5. 可選：勾選「Reset Sequence」重置文件序號
6. 可選：勾選「Reset Statistics」重置統計值
7. **建議先勾選「Preview Only」預覽影響範圍**
8. 確認無誤後，取消勾選「Preview Only」執行初始化

### 刪除整個 Tenant

適用場景：完全移除一個 Tenant

1. 以 **System Admin** 登入（Client = System）
2. 執行 **Delete Client (Tenant)** Process
3. 選擇 **Target Client**（要刪除的 Tenant）
4. **預設為預覽模式**，會顯示將被刪除的資料統計
5. 確認無誤後：
   - 取消勾選「Preview Only」
   - 勾選「Confirm Delete」確認刪除
6. 執行刪除

> ⚠️ **警告**：刪除操作不可逆，執行前請務必備份資料庫！

## 成功執行範例

### Delete Client 成功輸出

```
========================================
     刪除 Tenant 作業
========================================

目標 Tenant: MyCompany (ID=1000091)

【執行刪除】開始刪除 Tenant: MyCompany
----------------------------------------
  I_ElementValue: 刪除 111 筆
  M_Product_Acct: 刪除 31 筆
  AD_Workflow_Access: 刪除 68 筆
  C_PaymentTerm_Trl: 刪除 4 筆
  C_Campaign_Trl: 刪除 1 筆
  AD_PrintForm: 刪除 1 筆
  C_TaxCategory_Trl: 刪除 2 筆
  C_BP_Group_Acct: 刪除 4 筆
  AD_InfoWindow_Access: 刪除 29 筆
  AD_Preference: 刪除 4 筆
  M_Warehouse_Acct: 刪除 4 筆
  AD_OrgInfo: 刪除 3 筆
  M_ProductPrice: 刪除 81 筆
  PP_Product_BOM_Trl: 刪除 5 筆
  PP_Product_BOMLine_Trl: 刪除 13 筆
  AD_PrintFormatItem_Trl: 刪除 375 筆
  AD_PrintFormatItem: 刪除 375 筆
  C_DocType_Trl: 刪除 47 筆
  C_Tax_Trl: 刪除 3 筆
  GL_Category_Trl: 刪除 12 筆
  C_Cycle: 刪除 1 筆
  AD_TreeNodePR: 刪除 32 筆
  C_OrderLine: 刪除 7 筆
  C_Order: 刪除 2 筆
  AD_ChangeLog: 刪除 1 筆
  C_BP_Customer_Acct: 刪除 18 筆
  C_Activity_Trl: 刪除 1 筆
  PP_Product_BOMLine: 刪除 13 筆
  PP_Product_BOM: 刪除 5 筆
  M_Cost: 刪除 31 筆
  M_CostElement: 刪除 2 筆
  AD_Role_OrgAccess: 刪除 7 筆
  AD_Process_Access: 刪除 712 筆
  AD_PInstance_Para: 刪除 5 筆
  AD_PInstance: 刪除 1 筆
  AD_Session: 刪除 2 筆
  AD_UserPreference: 刪除 1 筆
  M_PriceList_Version_Trl: 刪除 3 筆
  C_CashBook_Acct: 刪除 1 筆
  C_CashBook: 刪除 1 筆
  AD_User_Roles: 刪除 5 筆
  C_Tax_Acct: 刪除 3 筆
  C_Tax: 刪除 3 筆
  C_AcctProcessor: 刪除 1 筆
  C_AcctSchema_Default: 刪除 1 筆
  AD_TreeNodeBP: 刪除 19 筆
  AD_Window_Access: 刪除 513 筆
  AD_Issue: 刪除 9 筆
  C_BankAccount_Acct: 刪除 1 筆
  C_BP_Vendor_Acct: 刪除 18 筆
  C_AcctSchema_Element: 刪除 4 筆
  AD_Document_Action_Access: 刪除 1316 筆
  C_DocType: 刪除 47 筆
  AD_Sequence: 刪除 183 筆
  GL_Category: 刪除 12 筆
  AD_Role: 刪除 2 筆
  C_ElementValue_Trl: 刪除 166 筆
  M_Product_Category_Acct: 刪除 7 筆
  AD_TreeNode: 刪除 175 筆
  R_RequestProcessor: 刪除 1 筆
  C_PeriodControl: 刪除 384 筆
  C_ValidCombination: 刪除 53 筆
  C_Project: 刪除 1 筆
  C_Activity: 刪除 1 筆
  M_PriceList_Version: 刪除 3 筆
  C_ElementValue: 刪除 166 筆
  C_BankAccount: 刪除 1 筆
  C_Element: 刪除 1 筆

--- 刪除 Client 記錄 ---
  AD_ClientInfo: 刪除 1 筆
  AD_Client: 刪除 1 筆

========================================
刪除完成！
  - 已刪除表數量: 68
  - 已刪除記錄數: 4521
========================================
```

## 故障排除

### PackIn 失敗清除腳本

如果 PackIn 執行失敗或需要重新匯入，請先執行以下 SQL 清除已匯入的資料：

```sql
-- ============================================
-- iDempiere Tenant Delete Plugin 清除腳本
-- 執行此腳本後可重新執行 PackIn
-- ============================================

-- 1. 刪除 Process Instance 相關資料
DELETE FROM AD_PInstance_Para WHERE AD_PInstance_ID IN
    (SELECT AD_PInstance_ID FROM AD_PInstance WHERE AD_Process_ID IN
        (SELECT AD_Process_ID FROM AD_Process WHERE AD_Process_UU IN
            ('f8c7b6a5-4d3e-2f1a-0b9c-8d7e6f5a4b3c',
             'e7d6c5b4-3a2f-1e0d-9c8b-7a6f5e4d3c2b',
             'a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d')));

DELETE FROM AD_PInstance_Log WHERE AD_PInstance_ID IN
    (SELECT AD_PInstance_ID FROM AD_PInstance WHERE AD_Process_ID IN
        (SELECT AD_Process_ID FROM AD_Process WHERE AD_Process_UU IN
            ('f8c7b6a5-4d3e-2f1a-0b9c-8d7e6f5a4b3c',
             'e7d6c5b4-3a2f-1e0d-9c8b-7a6f5e4d3c2b',
             'a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d')));

DELETE FROM AD_PInstance WHERE AD_Process_ID IN
    (SELECT AD_Process_ID FROM AD_Process WHERE AD_Process_UU IN
        ('f8c7b6a5-4d3e-2f1a-0b9c-8d7e6f5a4b3c',
         'e7d6c5b4-3a2f-1e0d-9c8b-7a6f5e4d3c2b',
         'a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d'));

-- 2. 刪除 Menu 項目
DELETE FROM AD_Menu WHERE AD_Process_ID IN
    (SELECT AD_Process_ID FROM AD_Process WHERE AD_Process_UU IN
        ('f8c7b6a5-4d3e-2f1a-0b9c-8d7e6f5a4b3c',
         'e7d6c5b4-3a2f-1e0d-9c8b-7a6f5e4d3c2b',
         'a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d'));

-- 3. 刪除 Process Access 權限
DELETE FROM AD_Process_Access WHERE AD_Process_ID IN
    (SELECT AD_Process_ID FROM AD_Process WHERE AD_Process_UU IN
        ('f8c7b6a5-4d3e-2f1a-0b9c-8d7e6f5a4b3c',
         'e7d6c5b4-3a2f-1e0d-9c8b-7a6f5e4d3c2b',
         'a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d'));

-- 4. 刪除 Process Parameters
DELETE FROM AD_Process_Para WHERE AD_Process_ID IN
    (SELECT AD_Process_ID FROM AD_Process WHERE AD_Process_UU IN
        ('f8c7b6a5-4d3e-2f1a-0b9c-8d7e6f5a4b3c',
         'e7d6c5b4-3a2f-1e0d-9c8b-7a6f5e4d3c2b',
         'a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d'));

-- 5. 刪除 Process
DELETE FROM AD_Process WHERE AD_Process_UU IN
    ('f8c7b6a5-4d3e-2f1a-0b9c-8d7e6f5a4b3c',
     'e7d6c5b4-3a2f-1e0d-9c8b-7a6f5e4d3c2b',
     'a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d');

-- 6. 刪除 Reference Table 定義
DELETE FROM AD_Ref_Table WHERE AD_Reference_ID IN
    (SELECT AD_Reference_ID FROM AD_Reference
     WHERE AD_Reference_UU = 'd4e5f6a7-8b9c-0d1e-2f3a-4b5c6d7e8f9a');

-- 7. 刪除 Reference
DELETE FROM AD_Reference WHERE AD_Reference_UU = 'd4e5f6a7-8b9c-0d1e-2f3a-4b5c6d7e8f9a';

-- 完成後重新啟動 iDempiere 並執行 PackIn
```

### 常見錯誤

#### 1. "Failed to create new process instance"
**原因**：插件 JAR 未正確部署或 OSGi 服務未註冊

**解決方案**：
1. 確認 JAR 已複製到 `plugins/` 目錄
2. 重啟 iDempiere
3. 檢查 OSGi console：`ss | grep clientinit`

#### 2. "Record not deleted - there are dependent records"
**原因**：外鍵依賴順序問題

**解決方案**：已在最新版本修正，更新到最新版本即可

#### 3. "必須以 System Client 登入才能執行此 Process"
**原因**：未以正確的 Client 登入

**解決方案**：登出後以 System Admin (Client = System) 重新登入

#### 4. Target Client 下拉選單沒有選項
**原因**：AD_Reference 設定問題

**解決方案**：
1. 執行上方的清除 SQL
2. 重新執行 PackIn
3. 確認 `AD_Reference_ID = 19 (Table)` 而非 `18 (Table Direct)`

## 技術架構

```
tw.idempiere.clientinit/
├── src/
│   └── tw/idempiere/clientinit/
│       ├── Activator.java              # OSGi 啟動器
│       ├── ProcessFactory.java         # Process 工廠
│       ├── process/
│       │   ├── InitializeClientProcess.java   # 初始化 Process
│       │   ├── InitializeClientPreview.java   # 預覽 Process
│       │   └── DeleteClientProcess.java       # 刪除 Process
│       ├── data/
│       │   ├── ForeignKeyAnalyzer.java        # FK 依賴分析
│       │   ├── TableDependency.java           # 表依賴定義
│       │   └── TransactionTables.java         # 交易表定義
│       ├── cleanup/
│       │   ├── TransactionCleanup.java        # 交易清理
│       │   ├── SequenceReset.java             # 序號重置
│       │   └── StatisticsReset.java           # 統計值重置
│       └── util/
│           └── CleanupLog.java                # 清理日誌
├── META-INF/
│   └── MANIFEST.MF                     # OSGi 配置
├── OSGI-INF/
│   ├── component.xml                   # OSGi 組件
│   └── ProcessFactory.xml              # ProcessFactory 服務
├── 2pack/
│   └── ClientInitProcess/
│       └── dict/
│           └── PackOut.xml             # Application Dictionary
└── pom.xml                             # Maven 構建配置
```

## 授權

MIT License

## 作者

- iDempiere Community
- Co-Authored-By: Claude Opus 4.5

/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.cupcake

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cupcake.data.DataSource
import com.example.cupcake.data.OrderUiState
import com.example.cupcake.ui.OrderSummaryScreen
import com.example.cupcake.ui.OrderViewModel
import com.example.cupcake.ui.SelectOptionScreen
import com.example.cupcake.ui.StartOrderScreen

/**
 * enum values that represent the screens in the app
 * 枚举类添加构造器参数
 * @StringRes加这个注解作用是说明构造器参数是字符串资源ID
 * 枚举定义方式是：enum class
 */
enum class CupcakeScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Flavor(title = R.string.choose_flavor),
    Pickup(title = R.string.choose_pickup_date),
    Summary(title = R.string.order_summary)
}

/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 * 定义App导航栏
 */
@Composable
fun CupcakeAppBar(
    // 当前屏幕
    currentScreen: CupcakeScreen,
    // 导航是否能返回
    canNavigateBack: Boolean,
    // 返回导航事件
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 顶部菜单栏
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            // 如果能返回，则添加返回图标，并给图标添加返回事件
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

// App入口
// 有一个问题，页面如何获取导航器与ViewModel呢？ todo...
@Composable
fun CupcakeApp(
    // 状态注入方式
    // 引入ViewModel模型，组件通过此读取状态数据与调用状态提供的方法更新状态
    viewModel: OrderViewModel = viewModel(),
    // 导航控制器，提供路由跳转功能
    // 页面如何获取导航器
    navController: NavHostController = rememberNavController()
) {
    // Get current back stack entry
    // 通过导航控制器，获取当前路由返回栈
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = CupcakeScreen.valueOf(
        // 当backStackEntry为null时，则返回null，否则返回backStackEntry?.destination
        // ?.依此类型
        // ?:是backStackEntry?.destination?.route为null时，则取值为CupcakeScreen.Start.name
        backStackEntry?.destination?.route ?: CupcakeScreen.Start.name
    )

    Scaffold(
        // 通过Scaffold设置导航栏
        topBar = {
            CupcakeAppBar(
                // 传入当前屏幕作用是读取标题
                currentScreen = currentScreen,
                // 是否能返回，根据导航控制器 previousBackStackEntry 是否为空来判断
                canNavigateBack = navController.previousBackStackEntry != null,
                // 传递返回事件，注意导航器提供的方法navigateUp是对应返回事件，类似goBack
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        // 从状态流中读取最新的状态值，这里用到了委托关键字 by
        // 状态uiState取值时，实际是委托 runtime.getValue 方法来读取状态值的
        // 状态读取需要用到委托
        val uiState by viewModel.uiState.collectAsState()

        // 路由配置：builder: NavGraphBuilder.() -> Unit
        // 当前示例适用于一个主从页面内部组件的跳转，参数传递路由跳转都在父组件内完成
        NavHost(
            // 路由控制器
            navController = navController,
            // 开始路由名字
            startDestination = CupcakeScreen.Start.name,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            // 路由配置，通过 route 配置路由名字
            // 路由配置前要加 composable，route指定名字，那么navController就可以使用这个route进行路由跳转了
            // 枚举名
            composable(route = CupcakeScreen.Start.name) {
                // 当路由跳转到 CupcakeScreen.Start.name 时会显示页面 StartOrderScreen
                StartOrderScreen(
                    // 数据入参
                    quantityOptions = DataSource.quantityOptions,
                    // 下一个页面按钮事件
                    onNextButtonClicked = {
                        // 先更新模型状态
                        viewModel.setQuantity(it)
                        // 再跳转口味页面
                        navController.navigate(route=CupcakeScreen.Flavor.name)

                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_medium))
                )
            }
            composable(route = CupcakeScreen.Flavor.name) {
                // 口味页面
                val context = LocalContext.current
                    // 列表选择页面
                    SelectOptionScreen(
                        subtotal = uiState.price,
                        // 导航到下一个页面，navigate如何传参数，如何接收参数
                        onNextButtonClicked = { navController.navigate(CupcakeScreen.Pickup.name) },
                        // 增加取消按钮
                        onCancelButtonClicked = {
                            // 口味页返回首页的方法
                            cancelOrderAndNavigateToStart(viewModel, navController)
                        },
                        // flavors 是字符串为何要调用 context.resources.getString
                        // 原来flavors只是配置strings.xml中的id
                        // 所以此处要通过context.resources.getString获取字符串值
                        options = DataSource.flavors.map { id ->
                            context.resources.getString(id)
                        },
                        onSelectionChanged = { viewModel.setFlavor(it) },
                        modifier = Modifier.fillMaxHeight()
                    )

            }
            composable(route = CupcakeScreen.Pickup.name) {
                SelectOptionScreen(
                    subtotal = uiState.price,
                    onNextButtonClicked = { navController.navigate(CupcakeScreen.Summary.name) },
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController)
                    },
                    options = uiState.pickupOptions,
                    onSelectionChanged = { viewModel.setDate(it) },
                    modifier = Modifier.fillMaxHeight()
                )
            }
            composable(route = CupcakeScreen.Summary.name) {
                val context = LocalContext.current
                OrderSummaryScreen(
                    orderUiState = uiState,
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController)
                    },
                    onSendButtonClicked = { subject: String, summary: String ->
                        shareOrder(context, subject = subject, summary = summary)
                    },
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}

/**
 * Resets the [OrderUiState] and pops up to [CupcakeScreen.Start]
 */
private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
) {
    // 返回首页时要重置状态
    viewModel.resetOrder()
    // 返回到目标页
    navController.popBackStack(CupcakeScreen.Start.name, inclusive = false)
}

/**
 * Creates an intent to share order details
 * 发起分享
 */
private fun shareOrder(context: Context, subject: String, summary: String) {
    // Create an ACTION_SEND implicit intent with order details in the intent extras
    // Intent.ACTION_SEND 分享
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, summary)
    }
    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.new_cupcake_order)
        )
    )
}

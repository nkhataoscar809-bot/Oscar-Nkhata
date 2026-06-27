package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.database.ProductEntity
import com.example.ui.theme.*
import com.example.viewmodel.YovxViewModel

@Composable
fun MarketplaceScreen(
    viewModel: YovxViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsState()
    val activeCategory by viewModel.searchCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var activeProductToView by remember { mutableStateOf<ProductEntity?>(null) }
    var showOrderCompletedOverlay by remember { mutableStateOf(false) }

    // Client-side filtering logic matching the category and search term in Room
    val filteredProducts = products.filter { product ->
        val matchesCategory = activeCategory == "All" || product.category.equals(activeCategory, ignoreCase = true)
        val matchesQuery = searchQuery.trim().isEmpty() ||
                product.title.contains(searchQuery, ignoreCase = true) ||
                product.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Scaffold(
        modifier = modifier.fillMaxSize().background(YovxObsidian),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = YovxPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 80.dp)
                    .testTag("add_listing_fab")
            ) {
                Icon(imageVector = Icons.Default.AddBusiness, contentDescription = "List Product")
            }
        },
        containerColor = YovxObsidian
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Marketplace Header & Search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(YovxCharcoal)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Marketplace",
                    color = YovxTextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search products, gear, items...", fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear text")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YovxTextPrimary,
                        unfocusedTextColor = YovxTextPrimary,
                        focusedBorderColor = YovxPrimary,
                        unfocusedBorderColor = YovxGrey,
                        focusedContainerColor = YovxObsidian,
                        unfocusedContainerColor = YovxObsidian
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("marketplace_search")
                )
            }

            // Categories horizontal picker list (All, Electronics, Fashion, etc.)
            val categoriesList = listOf("All", "Electronics", "Fashion", "Automotive", "Sports")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(YovxCharcoal)
                    .padding(vertical = 12.dp)
                    .border(0.5.dp, Color.Transparent),
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categoriesList) { category ->
                    val isSelected = activeCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) YovxPrimary else YovxGrey.copy(alpha = 0.5f))
                            .clickable { viewModel.searchCategory.value = category }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("category_$category")
                    ) {
                        Text(
                            text = category,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Products Grid View
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "No products",
                            tint = YovxTextSecondary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No listings found matching your search.",
                            color = YovxTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductItemCard(
                            product = product,
                            onClick = { activeProductToView = product }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(70.dp)) // padding for Bottom navigation bar
        }

        // Create Listing Dialog Modal
        if (showCreateDialog) {
            CreateListingDialog(
                viewModel = viewModel,
                onDismiss = { showCreateDialog = false }
            )
        }

        // Product Details Modal Overlay
        if (activeProductToView != null) {
            ProductDetailsDialog(
                product = activeProductToView!!,
                onBuy = {
                    viewModel.updateProduct(activeProductToView!!.copy(isSold = true))
                    activeProductToView = null
                    showOrderCompletedOverlay = true
                },
                onDismiss = { activeProductToView = null },
                onViewSellerProfile = { sellerName ->
                    val userId = when (sellerName) {
                        "Sophia Martinez" -> "sophia_travel"
                        "Marcus Kitchen" -> "chef_marcus"
                        "Oscar Nkhata" -> "me"
                        "Yovx Merch" -> "yovx_official"
                        else -> null
                    }
                    if (userId != null) {
                        viewModel.selectedUserIdForView.value = userId
                        viewModel.navigateTo("Profile")
                    }
                }
            )
        }

        // Custom Order Completed popup overlay
        if (showOrderCompletedOverlay) {
            Dialog(onDismissRequest = { showOrderCompletedOverlay = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    colors = CardDefaults.cardColors(containerColor = YovxCharcoal),
                    border = BorderStroke(1.dp, YovxPrimary)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(YovxGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Order Placed Successfully!",
                            color = YovxTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your simulated purchase has been finalized. The seller has been notified via Yovx Direct Messaging thread.",
                            color = YovxTextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { showOrderCompletedOverlay = false },
                            colors = ButtonDefaults.buttonColors(containerColor = YovxPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Awesome", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(6.dp)
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("product_item_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x1F252530)),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x05FFFFFF)))
        )
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Product cover photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                // Category badge overlay
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(text = product.category, color = Color.White, fontSize = 10.sp)
                }

                // Sold Out overlay
                if (product.isSold) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color.Black.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SOLD OUT",
                            color = YovxPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.title,
                    color = YovxTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        color = YovxPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = YovxGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${product.sellerRating}",
                            color = YovxTextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = YovxTextSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = product.location,
                        color = YovxTextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dialog where users can input information to create a new product listing.
 */
@Composable
fun CreateListingDialog(
    viewModel: YovxViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Electronics") }
    var imageUrl by remember { mutableStateOf("") }

    val categories = listOf("Electronics", "Fashion", "Automotive", "Sports")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            colors = CardDefaults.cardColors(containerColor = YovxCharcoal),
            border = BorderStroke(1.dp, YovxGrey)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "List New Product",
                        color = YovxTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = YovxTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Product Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YovxTextPrimary,
                        unfocusedTextColor = YovxTextPrimary,
                        focusedBorderColor = YovxPrimary,
                        unfocusedBorderColor = YovxGrey
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("new_product_title")
                )

                // Price Input
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YovxTextPrimary,
                        unfocusedTextColor = YovxTextPrimary,
                        focusedBorderColor = YovxPrimary,
                        unfocusedBorderColor = YovxGrey
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Category selection dropdown feel
                Text(
                    text = "Category",
                    color = YovxTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) YovxPrimary else YovxGrey)
                                .clickable { category = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = cat, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detailed Description") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YovxTextPrimary,
                        unfocusedTextColor = YovxTextPrimary,
                        focusedBorderColor = YovxPrimary,
                        unfocusedBorderColor = YovxGrey
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Image URL Input
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL (Unsplash/Direct Link)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = YovxTextPrimary,
                        unfocusedTextColor = YovxTextPrimary,
                        focusedBorderColor = YovxPrimary,
                        unfocusedBorderColor = YovxGrey
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val price = priceStr.toDoubleOrNull() ?: 0.0
                        viewModel.createMarketplaceListing(title, description, price, category, imageUrl)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = YovxPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_product_button")
                ) {
                    Text(text = "Publish Listing", color = Color.White)
                }
            }
        }
    }
}

/**
 * Detail modal of selected marketplace product.
 */
@Composable
fun ProductDetailsDialog(
    product: ProductEntity,
    onBuy: () -> Unit,
    onDismiss: () -> Unit,
    onViewSellerProfile: (String) -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = YovxCharcoal),
                border = BorderStroke(0.5.dp, YovxGrey)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(product.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Product focus visual",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close details", tint = Color.White)
                        }
                    }

                    // Content details
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = product.title,
                            color = YovxTextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$${String.format("%.2f", product.price)}",
                                color = YovxPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Loc",
                                    tint = YovxTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = product.location,
                                    color = YovxTextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = YovxGrey, modifier = Modifier.padding(vertical = 12.dp))

                        // Description
                        Text(
                            text = "About This Item",
                            color = YovxTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = product.description,
                            color = YovxTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        HorizontalDivider(color = YovxGrey, modifier = Modifier.padding(vertical = 12.dp))

                        // Seller Profile Row
                        Text(
                            text = "Seller Information",
                            color = YovxTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    onViewSellerProfile(product.sellerName)
                                    onDismiss()
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(product.sellerAvatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Seller Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = product.sellerName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = "Rating", tint = YovxGold, modifier = Modifier.size(12.dp))
                                    Text(text = "${product.sellerRating} Merchant Rating", color = YovxTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                                }
                            }
                        }
                    }

                    // Action buy panel bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .background(YovxCharcoal)
                            .padding(14.dp)
                    ) {
                        Button(
                            onClick = onBuy,
                            enabled = !product.isSold,
                            colors = ButtonDefaults.buttonColors(containerColor = YovxPrimary, disabledContainerColor = YovxGrey),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("purchase_product_button")
                        ) {
                            Text(
                                text = if (product.isSold) "SOLD OUT" else "Commit Simulated Purchase",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

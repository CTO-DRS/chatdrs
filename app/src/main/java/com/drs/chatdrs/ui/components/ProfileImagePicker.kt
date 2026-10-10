package com.drs.chatdrs.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.drs.chatdrs.ui.theme.CairoFont
import com.drs.chatdrs.ui.theme.ErrorRed
import com.drs.chatdrs.ui.theme.PurpleGradientEnd
import com.drs.chatdrs.ui.theme.PurpleGradientStart
import com.drs.chatdrs.ui.theme.PurplePrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileImagePicker(
    selectedImageUri: Uri?,
    selectedBitmap: Bitmap?,
    onImageSelected: (Uri?, Bitmap?) -> Unit,
    size: Dp = 104.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showSourceSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Android Zero-Permission Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(uri, null)
        }
    }

    // Camera preview capture launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            onImageSelected(null, bitmap)
        }
    }

    // Camera runtime permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            Toast.makeText(
                context,
                "يلزم إذن الكاميرا لالتقاط صورة الملف الشخصي",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val gradientBrush = Brush.linearGradient(
        listOf(PurpleGradientStart, PurpleGradientEnd)
    )

    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .size(size)
            .testTag("profile_image_picker_container"),
        contentAlignment = Alignment.Center
    ) {
        // Main Circle with complete circular clipping
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.5.dp, gradientBrush, CircleShape)
                .clickable { showSourceSheet = true }
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = PurplePrimary.copy(alpha = 0.25f),
                    spotColor = PurplePrimary.copy(alpha = 0.25f)
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                selectedImageUri != null -> {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "صورة الملف الشخصي",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(size)
                            .clip(CircleShape)
                    )
                }
                selectedBitmap != null -> {
                    Image(
                        bitmap = selectedBitmap.asImageBitmap(),
                        contentDescription = "صورة الملف الشخصي",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(size)
                            .clip(CircleShape)
                    )
                }
                else -> {
                    // Placeholder when no photo is selected
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(size)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        PurpleGradientStart.copy(alpha = 0.15f),
                                        PurpleGradientEnd.copy(alpha = 0.05f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "صورة افتراضية",
                            tint = PurplePrimary,
                            modifier = Modifier.size(size * 0.52f)
                        )
                    }
                }
            }
        }

        // Camera Action Badge at Bottom End
        Box(
            modifier = Modifier
                .size(34.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
                .clip(CircleShape)
                .border(2.5.dp, surfaceColor, CircleShape)
                .background(gradientBrush)
                .clickable { showSourceSheet = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = "تغيير الصورة",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }

    // Modal Sheet to select between Gallery, Camera, or Delete
    if (showSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSourceSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(42.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = "صورة الملف الشخصي",
                    fontFamily = CairoFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                // Option 1: Gallery (Zero-permission Android Photo Picker)
                SourceOptionRow(
                    icon = Icons.Outlined.PhotoLibrary,
                    title = "اختيار من المعرض",
                    subtitle = "اختيار صورة من استوديو الصور في جهازك",
                    onClick = {
                        coroutineScope.launch {
                            showSourceSheet = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    }
                )

                // Option 2: Camera
                SourceOptionRow(
                    icon = Icons.Outlined.PhotoCamera,
                    title = "التقاط صورة بالكاميرا",
                    subtitle = "التقاط صورة ذاتية جديدة مباشرة",
                    onClick = {
                        coroutineScope.launch {
                            showSourceSheet = false
                            val permissionCheck = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            )
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                takePictureLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    }
                )

                // Option 3: Remove photo (if selected)
                if (selectedImageUri != null || selectedBitmap != null) {
                    SourceOptionRow(
                        icon = Icons.Outlined.Delete,
                        title = "إزالة الصورة الحالية",
                        subtitle = "الرجوع إلى الصورة الافتراضية لحسابك",
                        tint = ErrorRed,
                        onClick = {
                            coroutineScope.launch {
                                onImageSelected(null, null)
                                showSourceSheet = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (tint == ErrorRed) ErrorRed.copy(alpha = 0.12f)
                    else PurplePrimary.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (tint == ErrorRed) ErrorRed else PurplePrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                fontFamily = CairoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = tint
            )
            Text(
                text = subtitle,
                fontFamily = CairoFont,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

///
/// Copyright © 2016-2024 The Thingsboard Authors
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///     http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { Component, Inject, OnInit, OnDestroy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DevicePingResponse, DevicePingService } from '@core/http/device-ping.service';

/**
 * Data interface for the ping dialog.
 */
export interface DevicePingDialogData {
  /** Device ID to ping */
  deviceId: string;
  /** Device name for display */
  deviceName: string;
}

/**
 * Dialog component for displaying device ping results.
 *
 * This component shows:
 * - Loading state while ping is in progress
 * - Success state with reachability status and details
 * - Error state with retry option
 *
 * @example
 * ```typescript
 * this.dialog.open(DevicePingDialogComponent, {
 *   data: { deviceId: 'uuid', deviceName: 'My Device' },
 *   width: '450px'
 * });
 * ```src/app/modules/home/components/device/device-ping-dialog.component.ts
 */
@Component({
  selector: 'tb-device-ping-dialog',
  templateUrl: './device-ping-dialog.component.html',
  styleUrls: ['./device-ping-dialog.component.scss']
})
export class DevicePingDialogComponent implements OnInit, OnDestroy {

  /** Current loading state */
  loading = true;

  /** Error message if ping failed */
  error: string | null = null;

  /** Ping result data */
  pingResult: DevicePingResponse | null = null;

  /** Subject for component cleanup */
  private destroy$ = new Subject<void>();

  constructor(
    private dialogRef: MatDialogRef<DevicePingDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DevicePingDialogData,
    private devicePingService: DevicePingService
  ) {}

  ngOnInit(): void {
    this.executePing();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Execute the ping request to check device reachability.
   * Can be called multiple times for retry functionality.
   */
  executePing(): void {
    this.loading = true;
    this.error = null;
    this.pingResult = null;

    this.devicePingService.pingDevice(this.data.deviceId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: DevicePingResponse) => {
          this.pingResult = response;
          this.loading = false;
        },
        error: (err: any) => {
          this.error = err.error?.message || err.message || 'Failed to ping device';
          this.loading = false;
        }
      });
  }

  /**
   * Close the dialog.
   */
  close(): void {
    this.dialogRef.close();
  }

  /**
   * Format the lastSeen timestamp for display.
   *
   * @param lastSeen - ISO 8601 timestamp string or null
   * @returns Formatted date string or 'Never' if null
   */
  formatLastSeen(lastSeen: string | null): string {
    if (!lastSeen) {
      return 'Never';
    }
    try {
      return new Date(lastSeen).toLocaleString();
    } catch {
      return 'Invalid date';
    }
  }

  /**
   * Format the inactivity duration for human-readable display.
   *
   * @param seconds - Number of seconds since last activity
   * @returns Human-readable string like "30 seconds ago" or "2 hours ago"
   */
  formatInactivity(seconds: number | null): string {
    if (seconds === null || seconds === undefined) {
      return 'Unknown';
    }

    if (seconds < 0) {
      return 'Unknown';
    }

    if (seconds < 60) {
      return `${seconds} second${seconds !== 1 ? 's' : ''} ago`;
    }

    if (seconds < 3600) {
      const minutes = Math.floor(seconds / 60);
      return `${minutes} minute${minutes !== 1 ? 's' : ''} ago`;
    }

    if (seconds < 86400) {
      const hours = Math.floor(seconds / 3600);
      return `${hours} hour${hours !== 1 ? 's' : ''} ago`;
    }

    const days = Math.floor(seconds / 86400);
    return `${days} day${days !== 1 ? 's' : ''} ago`;
  }

  /**
   * Get CSS class for status indicator based on reachability.
   *
   * @returns 'reachable' or 'unreachable' CSS class
   */
  getStatusClass(): string {
    return this.pingResult?.reachable ? 'reachable' : 'unreachable';
  }

  /**
   * Get icon name for status indicator.
   *
   * @returns Material icon name
   */
  getStatusIcon(): string {
    return this.pingResult?.reachable ? 'check_circle' : 'cancel';
  }

  /**
   * Get status text for display.
   *
   * @returns Human-readable status text
   */
  getStatusText(): string {
    return this.pingResult?.reachable ? 'Device is Reachable' : 'Device is Unreachable';
  }
}

---
name: TutorDesk Editorial Slate
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#45474c'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#75777d'
  outline-variant: '#c5c6cd'
  surface-tint: '#545f73'
  primary: '#091426'
  on-primary: '#ffffff'
  primary-container: '#1e293b'
  on-primary-container: '#8590a6'
  inverse-primary: '#bcc7de'
  secondary: '#0058be'
  on-secondary: '#ffffff'
  secondary-container: '#2170e4'
  on-secondary-container: '#fefcff'
  tertiary: '#00190e'
  on-tertiary: '#ffffff'
  tertiary-container: '#00301e'
  on-tertiary-container: '#00a472'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d8e3fb'
  primary-fixed-dim: '#bcc7de'
  on-primary-fixed: '#111c2d'
  on-primary-fixed-variant: '#3c475a'
  secondary-fixed: '#d8e2ff'
  secondary-fixed-dim: '#adc6ff'
  on-secondary-fixed: '#001a42'
  on-secondary-fixed-variant: '#004395'
  tertiary-fixed: '#6ffbbe'
  tertiary-fixed-dim: '#4edea3'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005236'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 38px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 17px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.005em
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.04em
  currency-stat:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
    letterSpacing: -0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style

This design system is engineered specifically for private tutors and academic coaching mentors managing HSC (Class XI-XII) ICT student batches across Bangladesh. The platform targets solo educators and small coaching centers who balance intense schedules, manual attendance tracking, and monthly tuition fee collection via mobile devices on the go.

The visual ethos blends **Modern Professionalism** with **Tactile Academic Clarity**. It conveys authority, academic rigor, and effortless bookkeeping without the sterile rigidity of generic enterprise software. By prioritizing immediate readability under direct sunlight and high touch target precision, it eliminates cognitive fatigue during fast-paced classroom transitions. The emotional tone is dependable, organized, calm, and unmistakably modern.

## Colors

The palette establishes an orderly, trustworthy light aesthetic utilizing a soft, cool base to reduce eye strain while preserving crisp structural hierarchy.

- **Primary Canvas & Surfaces**: The global background uses a cool grayish-blue tint (`#F8FAFC`). Elevated structural containers, student cards, and bottom sheets use pure white (`#FFFFFF`) to ensure separation from the canvas.
- **Deep Slate Navy (`#1E293B`)**: The primary brand anchor, applied to top app headers, primary action buttons, batch title typography, and prominent active navigation elements.
- **Interactive Blue Accent (`#3B82F6`)**: Reserved for secondary utility actions, active filters, selected calendar dates, and informational badges.
- **Academic Operational Status Palette**:
  - **Emerald Green (`#10B981`)**: Strict semantic indicator for "Paid" tuition fees, "Present" attendance logs, and completed batch goals.
  - **Warm Amber (`#F59E0B`)**: Represents "Partial Payment," "Pending Verification," or "Excused Absence."
  - **Coral Red (`#EF4444`)**: High-priority alert reserved for "Unpaid / Due" balances, "Absent" marks, and critical batch schedule conflicts.
- **Text & Neutral Tiers**: Text hierarchy is governed by Slate 900 (`#0F172A`) for primary headlines, Slate 700 (`#334155`) for readable body text, and Slate 500 (`#64748B`) for metadata, BDT labels, and inactive icon states. Border definitions utilize Slate 200 (`#E2E8F0`).

## Typography

The typographic hierarchy utilizes **Plus Jakarta Sans** for structural headers, KPI metric summaries, and page navigation anchors, providing a welcoming yet geometrically authoritative tone. **Inter** handles high-density operational data, body text, form elements, and status chips, ensuring maximum legibility across variable mobile screen densities.

### Formatting Rules for BDT (৳)
- Display the Bengali Taka symbol `৳` preceding or succeeding numerals with a non-breaking space (e.g., `৳ 2,500` or `3,000 ৳`).
- Currency values must always render in tabular numerals (`font-variant-numeric: tabular-nums`) to preserve optical column alignment in billing lists and KPI summary cards.
- Status values (e.g., `৳ 0 Due`) inherit the corresponding semantic color (`#10B981` or `#EF4444`) with semibold weight (`600`).

## Layout & Spacing

This design system follows a single-column, fluid mobile-first layout optimized for thumb-reach ergonomics on handheld screens (360px to 430px viewport width), scaling predictably into tablet viewports.

### Grid & Breakpoints
- **Mobile (< 640px)**: 4-column layout; gutter is `1rem` (16px); screen edge margins are fixed at `1rem` (16px).
- **Tablet / Expanded (640px – 1024px)**: 8-column layout; maximum centered container width of 720px; gutter `1.25rem` (20px); screen margins `1.5rem` (24px).

### Spacing Principles
- Compact structural vertical rhythms are sustained via `space-md` (`0.75rem`) between standard list cells and form fields to maximize vertical data density without visual clutter.
- Segmented control groups, card internal paddings, and KPI stat containers employ `space-lg` (`1rem`) to keep interactive touch targets comfortable (minimum 44x44px).

## Elevation & Depth

Visual hierarchy relies on crisp surface separation rather than heavy skeuomorphism. It combines subtle ambient drop shadows with low-contrast borders to ensure depth remains legible under bright outdoor conditions.

- **Level 0 (Canvas Base)**: Flat `#F8FAFC`. Zero elevation.
- **Level 1 (Card & Content Surface)**: Pure white `#FFFFFF` paired with a delicate border (`1px solid #E2E8F0`) and an ambient shadow: `0 1px 3px rgba(15, 23, 42, 0.04), 0 1px 2px rgba(15, 23, 42, 0.02)`. Used for batch overview cards, student directory items, and quick action bars.
- **Level 2 (Floating Action & Interactive Trays)**: Pure white `#FFFFFF` with border (`1px solid #CBD5E1`) and shadow: `0 4px 12px -1px rgba(15, 23, 42, 0.08), 0 2px 6px -1px rgba(15, 23, 42, 0.04)`. Used for bottom tab navigation, floating batch switcher popovers, and sticky attendance submit bars.
- **Level 3 (Modals & Bottom Drawers)**: Surface `#FFFFFF` elevated above a dimmed scrim (`rgba(15, 23, 42, 0.45)` backdrop blur of `4px`) accompanied by a top shadow: `0 -8px 24px rgba(15, 23, 42, 0.12)`.

## Shapes

The geometric personality features balanced, rounded curves (`0.5rem` base border-radius) that soften dense technical academic content while avoiding an overly casual aesthetic.

- **Standard Elements (`0.5rem` / 8px)**: Applied to input text boxes, student card containers, table headers, and modal action dialogs.
- **Container Elements (`1rem` / 16px)**: Applied to KPI stat summary modules, batch detail headers, and bottom sliding sheets.
- **Pill Elements (`9999px`)**: Reserved strictly for interactive switches, filter chips, attendance toggles (Present/Absent/Late), and status tags.

## Components

### Buttons & Primary Actions
- **Primary Button**: Background `#1E293B`, label `#FFFFFF`, height 48px, radius `0.5rem`. Active state scales to `0.98` with a deep slate press state (`#0F172A`).
- **Secondary / Ghost Button**: White background, 1px border `#CBD5E1`, text `#334155`.
- **Destructive Button**: Soft coral tint background (`#FEF2F2`), border `1px solid #FCA5A5`, text `#DC2626`.

### Segmented Attendance Switcher (Tactile Mobile Toggle)
A high-frequency classroom control designed for fast one-thumb roll-call marking:
- Container is styled with `#F1F5F9`, 4px inner padding, full pill radius.
- Three segments: **P (Present)**, **L (Late/Partial)**, **A (Absent)**.
- **Selected Present**: `#10B981` background, white bold text, subtle green glow.
- **Selected Late**: `#F59E0B` background, white bold text.
- **Selected Absent**: `#EF4444` background, white bold text.
- **Unselected**: Transparent background, text `#64748B`, font weight 500.

### KPI Stat Cards (Tuition & Batch Metrics)
- Stacked summary cards displaying real-time metrics (e.g., "Total Collected: ৳ 42,000", "Total Dues: ৳ 8,500").
- White background, 1px solid `#E2E8F0` border, `1rem` roundedness, `1rem` internal padding.
- Metric values use `currency-stat` typography with semantic color coding (Emerald for collections, Coral Red for outstanding student dues).

### Status Badges & Chips
- **Paid Badge**: `#ECFDF5` background, `#065F46` text, dot indicator `#10B981`.
- **Pending/Partial Badge**: `#FFFBEB` background, `#92400E` text, dot indicator `#F59E0B`.
- **Due/Overdue Badge**: `#FEF2F2` background, `#991B1B` text, dot indicator `#EF4444`.
- Shape: 100% pill radius, horizontal padding `0.5rem`, vertical padding `0.25rem`.

### Form Fields & BDT Inputs
- Input boxes feature a fixed 48px touch height with a crisp `#E2E8F0` resting border and pure white fill.
- Focus state activates a 2px slate-navy outline (`#1E293B`) without displacement.
- Currency inputs feature a fixed, non-editable leading badge displaying the `৳` symbol in `#64748B` with an internal hairline divider.

### Bottom Navigation Bar
- Fixed mobile footer with backdrop blur (`#FFFFFF` at 95% opacity), top hairline border (`1px solid #E2E8F0`), and 64px height.
- Tabs include: **Batches**, **Attendance**, **Fees/Collection**, **Students**.
- Active tab uses `#1E293B` icon and label with an indicator dot; inactive tabs use `#94A3B8`.
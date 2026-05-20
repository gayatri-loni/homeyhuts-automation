describe('Host blocks date from calendar', () => {

  // ─── Constants (kept in sync with BlockDateTest.java) ─────────────────────────
  const BLOCKED_DATE                      = '2026-11-27';   // td[data-date] / ISO format
  const UNIFIED_BLOCKED_DATE              = '27 Nov 26';    // Ant picker input (2-digit year required)
  const MANUAL_BLOCKED_CHECKIN_DATE       = '27 Nov 2026';  // Manual booking check-in input
  const MANUAL_BLOCKED_CHECKOUT_DATE      = '28 Nov 2026';  // Manual booking check-out input
  const PROPERTY_NAME                     = 'test House';
  const PROPERTY_ID                       = '5012';
  const GUEST_ROOM_ID                     = '5012';

  // Admin panel uses a different property for calendar/inventory verification
  const ADMIN_PROPERTY_ID                 = '4967';
  const ADMIN_BLOCKED_ARIA_LABEL          = 'November 27, 2026';
  const ADMIN_INVENTORY_DATE_KEY          = '2026-11-27';
  // Ant Design td[title] uses "27 Nov 2026" format in this app
  const DIRECT_BOOKING_BLOCKED_DATE_TITLE = '27 Nov 2026';

  beforeEach(() => {
    cy.viewport(1920, 1080);
    Cypress.on('uncaught:exception', () => false);
  });

  // ─── Helper: login and open Host Control Center ────────────────────────────────
  // Extracts the repetitive guest-portal login + "Host Control Center" redirect.
  // Returns after the browser lands on uat.host.homeyhutz.com.
  function loginAndOpenHostPortal() {
    cy.visit('https://uat.homeyhutz.com/signup-signin?redirectUrl=/&', { timeout: 30000 });
    cy.get('input[name="phoneOrEmail"]:visible').first().clear({ force: true }).type('9422121212');
    cy.contains('button', 'Continue').click();
    cy.wait(2000);
    cy.get('input[name="pin"]').type('123456');
    cy.wait(2000);
    cy.get('.space-y-6 > .ring-offset-white').click();
    cy.wait(2000);
    cy.get('.inline-flex > .flex').click();
    cy.wait(2000);
    cy.contains('Host Control Center').click();
    cy.wait(4000);
  }

  // ─── Helper: admin panel login ────────────────────────────────────────────────
  function loginAdmin() {
    cy.visit('https://uat.admin.new.homeyhutz.com/login');
    cy.get('#email').type('homey@admin.com');
    cy.get('#password').type('Password1!');
    cy.get('button[type="submit"]').click();
    cy.url().should('not.include', '/login');
    cy.wait(2000);
  }


  // ─── SETUP: Block the date once (idempotent) ──────────────────────────────────
  // Mirrors Java @BeforeClass blockDateOnce() / ensureDateBlocked():
  // click the date cell, then click Block ONLY if it is not already blocked.

  it('SETUP: Block calendar date (idempotent)', () => {
    loginAndOpenHostPortal();

    cy.origin(
      'https://uat.host.homeyhutz.com',
      { args: { BLOCKED_DATE, PROPERTY_ID } },
      ({ BLOCKED_DATE, PROPERTY_ID }) => {
        cy.contains('Properties').click();
        cy.wait(5000);

        cy.get('input[placeholder="Search by Property Name, Id, City"]')
          .should('be.visible').click().clear().focus().wait(500)
          .type(PROPERTY_ID, { delay: 100 });

        cy.contains('td', PROPERTY_ID).click();
        cy.wait(2000);
        cy.contains('Calendar & Pricing').click();
        cy.wait(2000);

        cy.get(`td[data-date="${BLOCKED_DATE}"]`).click();
        cy.wait(2000);

        // Idempotent: only click Block if date is NOT already blocked.
        // If "Unblock" text is present the date is already blocked — skip.
        cy.get('body').then($body => {
          const alreadyBlocked =
            $body.find('button, div, span').toArray()
              .some(el => el.textContent.trim() === 'Unblock');

          if (alreadyBlocked) {
            cy.log('ℹ️ Date is already blocked — skipping Block click');
            cy.get('body').type('{esc}');
          } else {
            cy.get('div.relative.z-10.flex-1.flex.items-center.justify-center.py-1.text-white')
              .should('be.visible')
              .click();
            cy.log('✅ Block clicked — date is now blocked');
          }
        });
      }
    );
  });


  // ─── TC1: Host property calendar shows blocked date ───────────────────────────
  // Mirrors Java TEST 1 (hostCalendarShowsBlockedDate):
  // click the date cell and assert the "Unblock" button is visible.

  it('TC1: Host property calendar shows blocked date', () => {
    loginAndOpenHostPortal();

    cy.origin(
      'https://uat.host.homeyhutz.com',
      { args: { BLOCKED_DATE, PROPERTY_ID } },
      ({ BLOCKED_DATE, PROPERTY_ID }) => {
        cy.contains('Properties').click();
        cy.wait(5000);

        cy.get('input[placeholder="Search by Property Name, Id, City"]')
          .should('be.visible').click().clear().focus().wait(500)
          .type(PROPERTY_ID, { delay: 100 });

        cy.contains('td', PROPERTY_ID).click();
        cy.wait(2000);
        cy.contains('Calendar & Pricing').click();
        cy.wait(2000);

        cy.get(`td[data-date="${BLOCKED_DATE}"]`).click();
        cy.wait(2000);

        // The "Unblock" action button confirms the date is blocked
        cy.contains('Unblock').should('be.visible');

        // Dismiss without toggling
        cy.get('body').type('{esc}');

        cy.log(`✅ Host property calendar correctly shows ${BLOCKED_DATE} as blocked`);
      }
    );
  });


  // ─── TC2: Verify blocked date in Unified Calendar ─────────────────────────────
  // Key: Ant picker input requires "27 Nov 26" (2-digit year); "27 Nov 2026" does NOT work.

  it('TC2: Verify blocked date in Unified Calendar', () => {
    loginAndOpenHostPortal();

    cy.origin(
      'https://uat.host.homeyhutz.com',
      { args: { UNIFIED_BLOCKED_DATE } },
      ({ UNIFIED_BLOCKED_DATE }) => {
        cy.wait(5000);
        cy.contains('Calendar').click();
        cy.wait(3000);

        cy.get('div.ant-picker-input').click();
        cy.wait(1000);

        cy.get('div.ant-picker-input input')
          .focus()
          .clear()
          .type(`${UNIFIED_BLOCKED_DATE}{enter}`);
        cy.wait(2000);

        cy.get('.uf_cal_date_box').then($dateBox => {
          // Primary visual indicator: outer bg-black/5 class + child linear-gradient overlay
          const isBlocked =
            $dateBox.hasClass('bg-black/5') &&
            $dateBox.find('div[style*="linear-gradient"]').length > 0;

          if (isBlocked) {
            cy.log(`✅ ${UNIFIED_BLOCKED_DATE} is BLOCKED in Unified Calendar (crossed)`);
          } else {
            cy.log(`❌ ${UNIFIED_BLOCKED_DATE} is NOT blocked in Unified Calendar`);
          }

          expect(isBlocked, 'Unified calendar date should be blocked').to.be.true;
        });
      }
    );
  });


  // ─── TC3: Verify blocked date in Manual Booking Calendar ─────────────────────
  // Submits a manual booking for the blocked date; expects the booking to be rejected.

  it('TC3: Verify blocked date in Manual Booking Calendar', () => {
    loginAndOpenHostPortal();

    cy.origin(
      'https://uat.host.homeyhutz.com',
      { args: { MANUAL_BLOCKED_CHECKIN_DATE, MANUAL_BLOCKED_CHECKOUT_DATE, PROPERTY_ID } },
      ({ MANUAL_BLOCKED_CHECKIN_DATE, MANUAL_BLOCKED_CHECKOUT_DATE, PROPERTY_ID }) => {
        cy.wait(5000);
        cy.contains('Reservation').click();
        cy.wait(3000);

        cy.get('button[type="button"]').contains('Create Manual Booking').click();
        cy.wait(2000);

        cy.get('input[placeholder="Search by Property Id, Name"]').type(PROPERTY_ID);
        cy.wait(2000);

        cy.get('.flex.items-center.p-2.border-b.cursor-pointer.hover\\:bg-gray-100').first().click();
        cy.wait(2000);

        cy.get('input#bookingContactDetail_name').clear().type('Test User');
        cy.get('input#bookingContactDetail_email').clear().type('testuser@example.com');
        cy.get('input#bookingContactDetail_phone').clear().type('9234567804');
        cy.get('input#totalAmount').clear().type('2000');
        cy.get('input#advanceAmount').clear().type('2000');

        cy.get('input#checkInOut[placeholder="Check In Date"]')
          .clear()
          .type(`${MANUAL_BLOCKED_CHECKIN_DATE}{enter}`);
        cy.wait(2000);

        cy.get('input[placeholder*="Check Out"]')
          .clear()
          .type(`${MANUAL_BLOCKED_CHECKOUT_DATE}{enter}`);
        cy.wait(2000);

        cy.get('button[type="submit"]').contains('Create Booking').click();
        cy.wait(3000);

        cy.get('body').then($body => {
          const hasError    = $body.find('.ant-message-error, .ant-notification-error, [class*="error"]').length > 0;
          const stillOnForm = $body.find('input#bookingContactDetail_name').length > 0;
          const hasSuccess  = $body.find('.ant-message-success, .ant-notification-success').length > 0;

          if (hasError || (stillOnForm && !hasSuccess)) {
            cy.log(`✅ Booking NOT created — ${MANUAL_BLOCKED_CHECKIN_DATE} is BLOCKED`);
            expect(true, 'Date is blocked, booking correctly prevented').to.be.true;
          } else {
            cy.log(`❌ Booking WAS created — ${MANUAL_BLOCKED_CHECKIN_DATE} is NOT BLOCKED`);
            expect(false, 'Date should be blocked but booking was created').to.be.true;
          }
        });
      }
    );
  });


  // ─── TC4: Verify blocked date in Channel Manager Calendar ────────────────────
  // Expects inventory = 0 for the blocked date.

  it('TC4: Verify blocked date in Channel Manager Calendar', () => {
    loginAndOpenHostPortal();

    cy.origin(
      'https://uat.host.homeyhutz.com',
      { args: { UNIFIED_BLOCKED_DATE, PROPERTY_ID } },
      ({ UNIFIED_BLOCKED_DATE, PROPERTY_ID }) => {
        cy.wait(5000);
        cy.contains('Channel Manager').click();
        cy.wait(3000);

        // Select the property tab — rendered as "P5012"
        cy.contains(`P${PROPERTY_ID}`).click();
        cy.wait(2000);

        cy.get('.ant-picker-input input[placeholder="Select date"]').click();
        cy.wait(1000);

        cy.get('.ant-picker-input input[placeholder="Select date"]')
          .clear()
          .type(`${UNIFIED_BLOCKED_DATE}{enter}`);
        cy.wait(2000);

        cy.get('input.ant-input-number-input[type="number"]').then($input => {
          const inventoryValue = $input.val();

          if (inventoryValue === '0') {
            cy.log(`✅ Channel Manager: ${UNIFIED_BLOCKED_DATE} is BLOCKED (inventory = 0)`);
            expect(inventoryValue, 'Inventory should be 0 (blocked)').to.equal('0');
          } else {
            cy.log(`❌ Channel Manager: ${UNIFIED_BLOCKED_DATE} is NOT BLOCKED (inventory = ${inventoryValue})`);
            throw new Error(`Test FAILED: inventory = ${inventoryValue}, expected 0`);
          }
        });
      }
    );
  });


  // ─── TC5: Verify blocked date in Admin Property Calendar ─────────────────────
  // Admin panel uses property 4967 (ADMIN_PROPERTY_ID).
  // Navigates the FullCalendar month by month until November 2026 is reached,
  // then checks for a "+more" blocked-event link on the 27th.

  it('TC5: Admin property calendar shows blocked date', () => {
    loginAdmin();

    cy.contains('.ant-menu-title-content', 'Properties').click();
    cy.wait(2000);

    cy.get('input[placeholder="Search by Name, Property Id"]')
      .clear()
      .type('4967', { delay: 300 });
    cy.wait(2000);

    cy.contains('td.ant-table-cell', '4967').click();
    cy.wait(5000);

    cy.contains('button', 'Calendar & Pricing').click();
    cy.wait(3000);

    // Navigate month-by-month until the toolbar title shows "November 2026"
    const navigateToNovember2026 = (attempt = 0) => {
      if (attempt > 30) throw new Error('Could not navigate to November 2026 on admin calendar');

      cy.get('.fc-toolbar-title, .fc-header-toolbar h2, .fc-toolbar h2').then($title => {
        const text = $title.text();
        if (text.includes('November') && text.includes('2026')) {
          cy.log(`✅ Admin calendar reached: ${text}`);
        } else {
          cy.get('button[title*="Next"], .fc-next-button').click();
          cy.wait(500);
          navigateToNovember2026(attempt + 1);
        }
      });
    };
    navigateToNovember2026();
    cy.wait(1000);

    cy.get(`a[aria-label="${ADMIN_BLOCKED_ARIA_LABEL}"]`)   // "November 27, 2026"
      .parents('td')
      .then($cell => {
        const moreLink = $cell.find('a.fc-daygrid-more-link.fc-more-link');

        if (moreLink.length > 0) {
          cy.log(`✅ Admin calendar: ${ADMIN_BLOCKED_ARIA_LABEL} is BLOCKED — "+more" link found`);
          cy.wrap(moreLink).should('contain.text', 'more');
        } else {
          cy.log(`❌ Admin calendar: ${ADMIN_BLOCKED_ARIA_LABEL} is NOT BLOCKED — no "+more" link`);
          throw new Error('Expected "+more" blocked-event link not found; date appears unblocked');
        }
      });
  });


  // ─── TC6: Verify blocked date in Admin Inventory & Rates ─────────────────────
  // Expects inventory = 0 for 2026-11-27 on property 5012.

  it('TC6: Admin Inventory & Rates shows 0 for blocked date', () => {
    loginAdmin();

    cy.contains('span.ant-menu-title-content', 'Inventory & Rates').click();
    cy.wait(3000);

    cy.get('input[placeholder="Search by Property Name"]')
      .clear()
      .type(PROPERTY_ID);
    cy.wait(5000);

    cy.contains('span.font-medium', PROPERTY_ID).click();
    cy.wait(3000);

    cy.get('input[placeholder="Select date"]')
      .clear({ force: true })
      .type(`${UNIFIED_BLOCKED_DATE}`, { force: true })
      .type('{enter}');
    cy.wait(3000);

    // Scroll the inventory input into view and assert value = 0 (blocked)
    cy.get(`input[id*="inventory_"][id*="${ADMIN_INVENTORY_DATE_KEY}"]`)   // "2026-11-27"
      .parents('.ant-input-number')
      .find('.ant-input-number-input-wrap')
      .click();

    cy.get(`input[id*="inventory_"][id*="${ADMIN_INVENTORY_DATE_KEY}"]`)
      .should('have.value', '0');

    cy.log(`✅ Admin Inventory & Rates: inventory = 0 for ${ADMIN_INVENTORY_DATE_KEY}`);
  });


  // ─── TC7: Verify blocked date is disabled in Admin Direct Booking Calendar ────
  // Creates a manual booking from the admin Reservations screen and asserts that
  // the check-in date cell carries the ant-picker-cell-disabled class.

  it('TC7: Blocked date is disabled in Admin Direct Booking Calendar', () => {
    loginAdmin();

    cy.contains('span.ant-menu-title-content', 'Reservations').click();
    cy.wait(3000);

    cy.contains('button', 'Create Booking').click();

    // Admin direct booking searches by PROPERTY_NAME, not ID
    cy.get('input[placeholder="Search by Property Id, Name"]')
      .clear()
      .type(PROPERTY_NAME);

    // Two-step dropdown selection (cy-confirmed selectors from Java comments)
    cy.contains('div.text-sm.font-medium.line-clamp-1', PROPERTY_NAME).click();

    cy.get('.flex.items-center.p-2.border-b.cursor-pointer.hover\\:bg-gray-100')
      .first()
      .click();
    cy.wait(2000);

    cy.get('input#bookingContactDetail_name').clear().type('Test User');
    cy.get('input#bookingContactDetail_email').clear().type('testuser@example.com');
    cy.get('input#bookingContactDetail_phone').clear().type('9876543210');
    cy.get('input#totalAmount').clear().type('2000');

    // Type the blocked check-in date to open the Ant Design date picker
    cy.get('input#checkInOut[placeholder="Check In Date"]')
      .clear()
      .type(`${MANUAL_BLOCKED_CHECKIN_DATE}{enter}`);   // "27 Nov 2026"
    cy.wait(1500);

    // The date cell must carry ant-picker-cell-disabled (test passes only if blocked)
    // td[title] can be either "27 Nov 2026" (app format) or ISO "2026-11-27"
    cy.get(`td[title="${DIRECT_BOOKING_BLOCKED_DATE_TITLE}"], td[title="${BLOCKED_DATE}"]`)
      .should('have.class', 'ant-picker-cell-disabled');

    cy.log(`✅ Admin direct booking calendar: ${DIRECT_BOOKING_BLOCKED_DATE_TITLE} is disabled`);
  });


  // ─── TC8: Verify blocked date in Guest-Facing Property Calendar ───────────────
  // Mirrors Java TEST 8 (verifyBlockedDateInGuestFacingCalendar).
  // Navigates directly to the property page with the blocked date as checkIn param.
  // Passes if the calendar cell is disabled OR an unavailability message is shown
  // OR no "Request to Book" / "Book Now" CTA is present.

  it('TC8: Guest-facing property calendar shows blocked date as unavailable', () => {
    cy.visit(
      `https://uat.homeyhutz.com/rooms/${GUEST_ROOM_ID}` +
      `?checkIn=${BLOCKED_DATE}&checkOut=2026-11-28&adults=1&children=0&infants=0&pets=0`,
      { timeout: 30000 }
    );
    cy.wait(3000);

    cy.get('body').then($body => {
      const bodyText = $body.text().toLowerCase();

      const dateCellDisabled =
        $body.find(`td[title="${BLOCKED_DATE}"].ant-picker-cell-disabled`).length > 0 ||
        $body.find(`td[data-date="${BLOCKED_DATE}"][class*="disabled"]`).length > 0;

      const unavailableText =
        bodyText.includes('unavailable') ||
        bodyText.includes('not available') ||
        bodyText.includes('blocked') ||
        bodyText.includes('dates are not available') ||
        bodyText.includes('sold out');

      const bookingCtaVisible =
        $body.find('button').toArray()
          .some(btn => {
            const t = btn.textContent.toLowerCase();
            return t.includes('request to book') || t.includes('book now') || t.includes('complete booking');
          });

      const noBookingCta = !bookingCtaVisible;

      cy.log(`dateCellDisabled=${dateCellDisabled}, unavailableText=${unavailableText}, noBookingCta=${noBookingCta}`);

      // If the calendar is not yet visible, try opening the date picker
      if (!dateCellDisabled && !unavailableText) {
        cy.get('.ant-picker, input[placeholder*="Check In"], input[placeholder*="check-in"]', { timeout: 5000 })
          .first()
          .click({ force: true });
        cy.wait(1500);

        cy.get('body').then($body2 => {
          const cellNowDisabled =
            $body2.find(`td[title="${BLOCKED_DATE}"].ant-picker-cell-disabled`).length > 0;

          expect(
            cellNowDisabled || unavailableText || noBookingCta,
            `Guest-facing property page should indicate ${BLOCKED_DATE} is unavailable`
          ).to.be.true;

          cy.log(`✅ Guest-facing calendar: ${BLOCKED_DATE} is shown as unavailable`);
        });
      } else {
        expect(
          dateCellDisabled || unavailableText || noBookingCta,
          `Guest-facing property page should indicate ${BLOCKED_DATE} is unavailable`
        ).to.be.true;

        cy.log(`✅ Guest-facing calendar: ${BLOCKED_DATE} is shown as unavailable`);
      }
    });
  });

});

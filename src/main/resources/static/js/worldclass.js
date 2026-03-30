document.addEventListener('DOMContentLoaded', () => {
    const panels = document.querySelectorAll('.wc-panel, .wc-hero, .wc-login');

    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (!entry.isIntersecting) {
                return;
            }
            entry.target.style.transform = 'translateY(0px)';
            entry.target.style.opacity = '1';
            observer.unobserve(entry.target);
        });
    }, { threshold: 0.15 });

    panels.forEach((panel, index) => {
        panel.style.opacity = '0';
        panel.style.transform = 'translateY(16px)';
        panel.style.transition = `opacity 0.55s ease ${index * 0.05}s, transform 0.55s ease ${index * 0.05}s`;
        observer.observe(panel);
    });

    const hero = document.querySelector('.wc-hero');
    if (hero) {
        window.addEventListener('mousemove', (event) => {
            const x = (event.clientX / window.innerWidth - 0.5) * 6;
            const y = (event.clientY / window.innerHeight - 0.5) * 6;
            hero.style.transform = `translate(${x * 0.16}px, ${y * 0.16}px)`;
        });
    }

    const realtimeForms = document.querySelectorAll('form[data-realtime-search="true"]');
    realtimeForms.forEach((form) => {
        const keywordInput = form.querySelector('input[name="keyword"]');
        if (!keywordInput) {
            return;
        }

        let typingTimer = null;
        let composing = false;

        const submitWithDebounce = () => {
            if (composing) {
                return;
            }
            if (typingTimer) {
                clearTimeout(typingTimer);
            }
            typingTimer = setTimeout(() => {
                form.requestSubmit();
            }, 300);
        };

        keywordInput.addEventListener('compositionstart', () => {
            composing = true;
        });

        keywordInput.addEventListener('compositionend', () => {
            composing = false;
            submitWithDebounce();
        });

        keywordInput.addEventListener('input', submitWithDebounce);

        const categorySelect = form.querySelector('select[name="categoryId"]');
        if (categorySelect) {
            categorySelect.addEventListener('change', () => form.requestSubmit());
        }

        const sortSelect = form.querySelector('select[name="sort"]');
        if (sortSelect) {
            sortSelect.addEventListener('change', () => form.requestSubmit());
        }
    });

    const cartBubble = document.querySelector('[data-cart-bubble="true"]');
    const addToCartForms = document.querySelectorAll('form[data-add-to-cart="true"]');

    if (cartBubble && addToCartForms.length > 0) {
        addToCartForms.forEach((form) => {
            form.addEventListener('submit', (event) => {
                if (form.dataset.submitting === 'true') {
                    return;
                }

                event.preventDefault();
                form.dataset.submitting = 'true';

                playFlyToCart(form, cartBubble);

                window.setTimeout(() => {
                    form.submit();
                }, 460);
            });
        });
    }
});

function playFlyToCart(form, cartBubble) {
    const card = form.closest('.wc-course-card, tr, .wc-panel');
    const sourceImage = card ? card.querySelector('img') : null;

    if (!sourceImage || !sourceImage.getBoundingClientRect) {
        return;
    }

    const sourceRect = sourceImage.getBoundingClientRect();
    const bubbleRect = cartBubble.getBoundingClientRect();

    const flyer = sourceImage.cloneNode(true);
    flyer.classList.add('wc-fly-to-cart');
    flyer.style.left = `${sourceRect.left}px`;
    flyer.style.top = `${sourceRect.top}px`;
    flyer.style.width = `${Math.max(44, sourceRect.width)}px`;
    flyer.style.height = `${Math.max(44, sourceRect.height)}px`;
    flyer.style.opacity = '0.92';
    flyer.style.transform = 'translate(0, 0) scale(1)';

    document.body.appendChild(flyer);

    const targetX = bubbleRect.left + bubbleRect.width / 2 - (sourceRect.left + sourceRect.width / 2);
    const targetY = bubbleRect.top + bubbleRect.height / 2 - (sourceRect.top + sourceRect.height / 2);

    requestAnimationFrame(() => {
        flyer.style.transition = 'transform 420ms cubic-bezier(0.2, 0.78, 0.18, 1), opacity 420ms ease';
        flyer.style.transform = `translate(${targetX}px, ${targetY}px) scale(0.15)`;
        flyer.style.opacity = '0.1';
    });

    window.setTimeout(() => {
        flyer.remove();
        cartBubble.classList.add('wc-hit');
        window.setTimeout(() => cartBubble.classList.remove('wc-hit'), 400);
    }, 440);
}

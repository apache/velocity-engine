; This is a little gimp script for creating the image
; headers. You need to edit the last five lines of this
; file to suit your needs. Need to add the image flatten,
; index, and save as gif. Right now you have to do that
; manually. Hopefully I can write something in java that
; produce an image that looks as good as this one. For
; now it's scheme ... Yuck!

; move this to your /usr/share/gimp/scripts directory
; or the equivalent on your machine.

(define (color-highlight color)
  (let ((r (car color))
	(g (cadr color))
	(b (caddr color)))
    (set! r (+ r (* (- 255 r) 0.75)))
    (set! g (+ g (* (- 255 g) 0.75)))
    (set! b (+ b (* (- 255 b) 0.75)))
    (list r g b)))

(define (script-fu-jakarta-logo text size font bg-color text-color)
  (let* ((img (car (gimp-image-new 256 256 RGB)))
	 (text-layer (car (gimp-text img -1 0 0 text 10 TRUE size PIXELS "*" font "*" "*" "*" "*")))
	 (width (car (gimp-drawable-width text-layer)))
	 (height (car (gimp-drawable-height text-layer)))
	 (bg-layer (car (gimp-layer-new img width height RGB_IMAGE "Background" 100 NORMAL)))
	 (highlight-layer (car (gimp-layer-copy text-layer TRUE)))
	 (shadow-layer (car (gimp-layer-new img width height RGBA_IMAGE "Shadow" 100 MULTIPLY)))
	 (old-fg (car (gimp-palette-get-foreground)))
	 (old-bg (car (gimp-palette-get-background))))
    (gimp-image-disable-undo img)
    (gimp-image-resize img width height 0 0)
    (gimp-image-add-layer img bg-layer 1)
    (gimp-image-add-layer img shadow-layer 1)
    (gimp-palette-set-background text-color)
    (gimp-layer-set-preserve-trans text-layer TRUE)
    (gimp-edit-fill img text-layer)
    (gimp-edit-clear img shadow-layer)
    (gimp-palette-set-background (color-highlight text-color))
    (gimp-palette-set-background bg-color)
    (gimp-drawable-fill bg-layer BG-IMAGE-FILL)
    (gimp-selection-layer-alpha img text-layer)
    (gimp-palette-set-background '(0 0 0))
    (gimp-selection-feather img 7.5)
    (gimp-edit-fill img shadow-layer)
    (gimp-selection-none img)
    (gimp-palette-set-foreground '(255 255 255))
    (gimp-layer-translate shadow-layer 3 3)
    (gimp-layer-set-name text-layer text)
    (gimp-palette-set-background old-bg)
    (gimp-palette-set-foreground old-fg)
    (gimp-image-enable-undo img)
    (gimp-display-new img)))

(script-fu-register "script-fu-jakarta-logo"
		    "<Toolbox>/Xtns/Script-Fu/Logos/Jakarta"
		    "Creates a simple Jakarta project header with a drop shadow"
		    "Jason van Zyl"
		    "Jason van Zyl"
		    "2000"
		    ""
		    SF-VALUE "Text String" "\"Velocity\""
		    SF-VALUE "Font Size (in pixels)" "90"
		    SF-VALUE "Font" "\"officina_bold\""
		    SF-COLOR "Background Color" '(255 255 255)
		    SF-COLOR "Text Color" '(02 50 100))

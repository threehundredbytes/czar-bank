<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
</head>
<body style="font-family: Roboto, sans-serif;">
<div class="email-wrapper" style="background: #fafafa;">
    <div class="email-content-wrapper" style="background: #ffffff; margin: 0 auto; max-width: 600px; border: 1px solid rgb(192, 192, 192); border-top: 4px solid #00bfff;">
        <div class="email-body" style="margin: 8px;">
            <div class="email-header" style="display: flex; justify-content: center;">
                <p class="header" style="font-weight: bolder; font-size: 40px;">czar-bank</p>
            </div>
            <br>
            <div class="email-content" style="display: flex; justify-content: center; font-size: 18px;">
                <div>
                    <p style="font-size: 18px;">
                        Hello, ${username},
                    </p>
                    <p style="font-size: 18px;">
                        To verify your account, please, confirm your e-mail address
                    </p>
                    <br>
                    <div class="email-verify-button-wrapper" style="display: flex; justify-content: center;">
                        <a href="${emailVerificationUrl}" target="_blank">
                            <button class="email-verify-button" style="border: solid 1px transparent; border-radius: 4px; background-color: #00bfff; color: #fff; font-size: 24px; width: 320px; height: 48px;">
                                Confirm e-mail address
                            </button>
                        </a>
                    </div>
                    <br>
                    <p style="font-size: 18px;">
                        Or confirm using the link:&nbsp;
                        <a href="${emailVerificationUrl}" target="_blank">
                            ${emailVerificationUrl}
                        </a>
                    </p>
                    <br>
                    <div class="email-support-wrapper" style="display: flex; justify-content: center;">
                        <div class="email-support" style="display: flex; justify-content: center;">
                            <div>
                                <p class="email-support-header" style="display: flex; justify-content: center; font-size: 24pt;">
                                    Support
                                </p>
                                <br>
                                <div>
                                    <p class="email-support-content" style="display: flex; justify-content: center; font-size: 18px;">If you have any questions, please, let us know</p>
                                    <p class="email-support-content" style="display: flex; justify-content: center; font-size: 18px;">Contact us:&nbsp;${supportEmailAddress}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="email-footer" style="background: #fafafa; display: flex; justify-content: center; padding: 16px; color: rgba(0,0,0,0.3); font-size: 18px;">
        The source code can be found at&nbsp;<a href="https://github.com/Dreadblade-dev/czar-bank">GitHub</a>
    </div>
</div>
</body>
</html>